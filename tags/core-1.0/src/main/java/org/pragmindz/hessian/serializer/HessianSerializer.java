package org.pragmindz.hessian.serializer;
/*
    Hessian4J - Java Hessian Library
    Copyright (C) 2008 PragMindZ
    http://www.pragmindz.org
    mailto://info@nubius.be

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
import org.pragmindz.hessian.serializer.helper.*;
import org.pragmindz.hessian.serializer.namer.Namer;
import org.pragmindz.hessian.serializer.namer.IdentityNamer;
import org.pragmindz.hessian.serializer.namer.ChainNamer;
import org.pragmindz.hessian.serializer.namer.WipeHibernateNamer;
import org.pragmindz.hessian.model.*;

import java.util.*;

/**
 * The serializer is thread safe, all threads can share the same serializer.
 * No serializing state is kept in the object. The repository can be shared.
 */
public class HessianSerializer
{
    private HelperRepository<HessianHelper> repo = new CachingHelperRepository<HessianHelper>();

    {
        repo.addHelper(new ObjectHelperDirect());  // Uses field access by default, change this by setFieldAccess() method.
        repo.addHelper(new ClassHelper());
        repo.addHelper(new StringHelper());
        repo.addHelper(new BooleanHelper());
        repo.addHelper(new ByteHelper());
        repo.addHelper(new ShortHelper());
        repo.addHelper(new IntegerHelper());
        repo.addHelper(new LongHelper());
        repo.addHelper(new FloatHelper());
        repo.addHelper(new DoubleHelper());
        repo.addHelper(new BigIntegerHelper());
        repo.addHelper(new BigDecimalHelper());
        repo.addHelper(new CharacterHelper());
        repo.addHelper(new DateHelper());
        repo.addHelper(new CollectionHelper());
        repo.addHelper(new MapHelper());
        repo.addHelper(new ColorHelper());
        repo.addHelper(new FontHelper());
        repo.addHelper(new EnumHelper());
        repo.addHelper(new LocaleHelper());
        repo.addHelper(new CurrencyHelper());
        repo.addHelper(new StringBufferHelper());
        repo.addHelper(new StackTraceElementHelper());
        repo.addHelper(new SqlDateHelper());
        repo.addHelper(new SqlTimeHelper());
        repo.addHelper(new SqlTimestampHelper());
        repo.addHelper(new InvocationTargetExceptionHelper());
    }

    private ChainNamer namer = new ChainNamer(new IdentityNamer());
    private boolean fieldAccess;

    public HessianValue serialize(Object aJavaObject)
    throws HessianSerializerException
    {
        // It is very important the object pool is a identity map. Do not change this.
        // The map should *not* use the equals method on the keys, but should
        // use equivalence of the references.
        final IdentityHashMap<Object, HessianValue> lObjectPool = new IdentityHashMap<Object, HessianValue>();
        final HashMap<Class, HessianClassdef> lClassdefPool = new HashMap<Class, HessianClassdef>();

        try
        {
            return serialize(aJavaObject, lObjectPool, lClassdefPool);
        }
        catch(HessianSerializerException e)
        {
            // We can try to augment the error message with a dump of the layout of the hessian object we already created at this point.
            // This can give some more context about what went wrong.
            String lMsg = String.format("Error while serializing (convert to hessian) an instance of class '%1$s'.", aJavaObject==null?"null (no class)":aJavaObject.getClass().getName());
            if(lObjectPool.containsKey(aJavaObject)) lMsg = String.format("Error while serializing (convert to hessian) an instance from class '%1$s' with (incomplete) layout:\n%2$s", aJavaObject==null?"null (no class)":aJavaObject.getClass().getName(), aJavaObject==null?"No dump available, null.":lObjectPool.get(aJavaObject).prettyPrint());
            throw new HessianSerializerException(lMsg, e);
        }
    }

    public HessianValue serialize(Object aJavaObject, Map<Object, HessianValue> aObjectPool, Map<Class, HessianClassdef> aClassdefPool)
    throws HessianSerializerException            
    {
        if(aJavaObject == null) return HessianNull.NULL;       
        else
        {
            final Class lObjectClass = aJavaObject.getClass();

            final HessianValue lFastResult = aObjectPool.get(aJavaObject);
            if(lFastResult != null)
            {
                // We already rendered this object in the past.
                // We just have to return the previous rendered value.
                return lFastResult;
            }
            else
            {
                // We did not encounter this object before.
                // We generate a new key and associate it with the object.
                if(lObjectClass.isArray()) return ArrayHelper.HELPER.serialize(aJavaObject, this, aObjectPool, aClassdefPool);
                else return serializeImplObject(aJavaObject, lObjectClass, aObjectPool, aClassdefPool);
            }
        }
    }

    private HessianValue serializeImplObject(Object aObj,  Class aObjClass, Map<Object, HessianValue> aObjectPool, Map<Class, HessianClassdef> aClassdefPool)
    throws HessianSerializerException
    {
        final HessianHelper lHelper = repo.findHelper(aObjClass);
        return lHelper.serialize(aObj, this, aObjectPool, aClassdefPool);
    }

    public Object deserialize(HessianValue aValue)
    throws HessianSerializerException
    {
        // It is very important the value pool is a identity map. Do not change this.
        // The map should *not* use the equals method on the keys, but should
        // use equivalence of the references.
        final IdentityHashMap<HessianValue, Object> lObjectPool = new IdentityHashMap<HessianValue, Object>();

        try
        {
            return deserialize(aValue, lObjectPool);
        }
        catch(HessianSerializerException e)
        {
            // Augment the error message with a dump of the object.
            throw new HessianSerializerException(String.format("Error while deserializing (creating a Java object from) a hessian value with layout:\n%1$s", aValue==null?"No dump available, null.":aValue.prettyPrint()), e);
        }
    }

    public Object deserialize(HessianValue aValue, Map<HessianValue, Object> aObjectPool)
    throws HessianSerializerException
    {
        if(aValue == null)
        {
            // Problem Houston, no-go.
            return null;
        }
        else
        {
            // Now we are talking business.
            // First we check if we didn't encounter this hessian before.
            final Object lFastResult = aObjectPool.get(aValue);
            if(lFastResult != null) return lFastResult;

            // We have to check for all valid hessian types.
            if(HessianNull.NULL.equals(aValue)) return null;
            else if(aValue instanceof HessianString) return repo.findHelper(String.class).deserialize(aValue, this, aObjectPool);
            else if(aValue instanceof HessianBoolean) return repo.findHelper(Boolean.class).deserialize(aValue, this, aObjectPool);
            else if(aValue instanceof HessianInteger) return repo.findHelper(Integer.class).deserialize(aValue, this, aObjectPool);
            else if(aValue instanceof HessianLong) return repo.findHelper(Long.class).deserialize(aValue, this, aObjectPool);
            else if(aValue instanceof HessianDouble) return repo.findHelper(Double.class).deserialize(aValue, this, aObjectPool);
            else if(aValue instanceof HessianDate) return repo.findHelper(Date.class).deserialize(aValue, this, aObjectPool);
            else if(aValue instanceof HessianList)
            {
                // First we test the specific case where a list
                // is used to represent a native java array.
                final HessianString lType = ((HessianList) aValue).getType();
                if(lType != null && lType.getValue() != null)
                {
                    final String lJavaType = namer.mapHessian2Java(lType.getValue());
                    if(lJavaType != null && lJavaType.startsWith("["))
                    {
                        // We have a native array type here.
                        // We cannot use the normal type system to get the
                        // helper because the native arrays are built-in and
                        // follow their own rules.
                        return ArrayHelper.HELPER.deserialize(aValue, this, aObjectPool);
                    }
                }

                // Normal general case.
                final HessianHelper lHelper = findHelper(lType, Collection.class);
                if(lHelper == null) throw new HessianSerializerException("Don't know how to handle HessianList, no helper found.");
                else return lHelper.deserialize(aValue, this, aObjectPool);
            }
            else if(aValue instanceof HessianMap)
            {
                final HessianString lType = ((HessianMap) aValue).getType();
                final HessianHelper lHelper = findHelper(lType, Map.class);
                if(lHelper == null) throw new HessianSerializerException("Don't know how to handle HessianMap, no helper found.");
                else return lHelper.deserialize(aValue, this, aObjectPool);
            }
            else if(aValue instanceof HessianObject)
            {
                final HessianClassdef lHesClass = ((HessianObject) aValue).getHessianClassdef();
                final HessianHelper lHelper = findHelper(lHesClass.getType(), null);
                if(lHelper == null) throw new HessianSerializerException("Don't know how to handle HessianObject, no helper found.");
                else return lHelper.deserialize(aValue, this, aObjectPool);
            }
            else throw new HessianSerializerException(String.format("Don't know how to deserialize a class: %1$s.", aValue.getClass().getName()));
        }
    }

    private HessianHelper findHelper(HessianString aType, Class aFallbackClass)
    throws HessianSerializerException
    {
        if(aType != null && aType.getValue() != null)
        {
            final String lClassName = namer.mapHessian2Java(aType.getValue());
            try
            {
                final Class lObjClass = Class.forName(lClassName);
                return repo.findHelper(lObjClass);
            }
            catch(ClassNotFoundException e)
            {
                throw new HessianSerializerException(String.format("Cannot find java class to represent hessian class: %1$s.", lClassName), e);
            }
        }
        else
        {
            if(aFallbackClass != null)
            {
                return repo.findHelper(aFallbackClass);
            }
            else throw new HessianSerializerException("Encountered corrupt hessian class (without class/type info) and there is no fallback class.");
        }
    }

    public HelperRepository<HessianHelper> getRepo()
    {
        return repo;
    }

    public void setRepo(HelperRepository<HessianHelper> repo)
    {
        this.repo = repo;
    }

    public ChainNamer getNamer()
    {
        return namer;
    }

    /**
     * Determines the way Java objects are inspected (by Java-beans introspection using the getters/setters or by direct field access).
     * 
     * @param aValue true for direct field access, false for use with getters/setters
     */
    public void setFieldAccess(boolean aValue)
    {
       repo.addHelper(aValue ? new ObjectHelperDirect() : new ObjectHelper());
       fieldAccess = aValue;
    }

    public boolean isFieldAccess()
    {
        return fieldAccess;
    }

    public void addNamer(Namer aNamer)
    {
        namer.add(aNamer);
    }
}