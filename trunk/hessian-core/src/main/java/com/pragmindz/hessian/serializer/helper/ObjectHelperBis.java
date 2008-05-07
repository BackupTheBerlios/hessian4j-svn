package com.pragmindz.hessian.serializer.helper;
/*
    Hessian4J - Java Hessian Library
    Copyright (C) 2008 PragMindZ
    http://www.pragmindz.com
    mailto://???

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
import com.pragmindz.hessian.model.HessianClassdef;
import com.pragmindz.hessian.model.HessianObject;
import com.pragmindz.hessian.model.HessianString;
import com.pragmindz.hessian.model.HessianValue;
import com.pragmindz.hessian.serializer.HessianConstruct;
import com.pragmindz.hessian.serializer.HessianSerialize;
import com.pragmindz.hessian.serializer.HessianSerializer;
import com.pragmindz.hessian.serializer.HessianSerializerException;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Looks for fields inside objects. Troubles might occur for shadowed fields with same name in different classes in the
 * inheritance tree. If this happens, serialization will probably go wrong.
 */
public class ObjectHelperBis
implements HessianHelper
{
    private Class helpedClass = Object.class;
    private static final String HESCONS = "_HESCONS_";

    private Map<Class, AnnotatedMethods> annotatedPool = new HashMap<Class, AnnotatedMethods>();

    private static class AnnotatedMethods
    {
        public Constructor cons;
        public Method serialize;

        public AnnotatedMethods(Constructor aCons, Method aSerialize)
        {
            cons = aCons;
            serialize = aSerialize;
        }
    }

    // Accessing a shared object should be synced.
    protected synchronized AnnotatedMethods getAnnotatedMethods(Class aClass)
    throws HessianSerializerException
    {
        AnnotatedMethods lResult = annotatedPool.get(aClass);
        if(lResult == null)
        {
            final Constructor lCons = getAnnotatedConstructor(aClass);
            final Method lMeth = getAnnotatedSerializingMethod(aClass);

            if((lMeth == null && lCons != null) || (lMeth != null && lCons == null))
                throw new HessianSerializerException(String.format("ObjectHelperBis found inconsistency in class: '%1$s'. If annotated methods are used, it should contain both @HessianConstruct and @HessianSerialize together.", aClass.getClass().getName()));

            lResult = new AnnotatedMethods(lCons, lMeth);
            annotatedPool.put(aClass, lResult);
        }
        return lResult;
    }

    protected List<Field> getFieldInfo(Class aClass)
    {
        final List<Field> lJavaFields = new LinkedList<Field>();
        Class lClassWalker = aClass;
        while (lClassWalker != null)
        {
            final Field[] lClassFields = lClassWalker.getDeclaredFields();
            for (Field lFld : lClassFields)
            {
                int lModif = lFld.getModifiers();
                if (!Modifier.isTransient(lModif) &&
                        !Modifier.isAbstract(lModif) &&
                        !Modifier.isStatic(lModif) &&
                        !Modifier.isFinal(lModif))
                {
                    lFld.setAccessible(true);
                    lJavaFields.add(lFld);
                }
            }
            lClassWalker = lClassWalker.getSuperclass();
        }
        return lJavaFields;
    }

    protected Method getAnnotatedSerializingMethod(Class aClass)
    {
        // Check if we have an annotated class.
        for(Method lMethod : aClass.getDeclaredMethods())
        {
            if(lMethod.isAnnotationPresent(HessianSerialize.class))
            {
                lMethod.setAccessible(true);
                return lMethod;
            }
        }
        return null;
    }

    protected Constructor getAnnotatedConstructor(Class aClass)
    {
        //Check if we have a class with an annotated constructor
        final Constructor[] lConstructors = aClass.getDeclaredConstructors();
        for(Constructor lCons : lConstructors)
            if(lCons.isAnnotationPresent(HessianConstruct.class))
            {
                // Found the constructor we are
                // looking for.
                lCons.setAccessible(true);
                return lCons;
            }
        return null;
    }

    public HessianValue serialize(Object aJavaObject, HessianSerializer aEngine, Map<Object, HessianValue> aObjectPool, Map<Class, HessianClassdef> aClassdefPool)
    throws HessianSerializerException
    {
        // First we have to cope with the special case of "writeReplace" objects.
        // It is described in the official specs of the serializable interface.
        ////////////////////////////////////////////////////////////////////////////
        if (aJavaObject instanceof Serializable)
        {
            try
            {
                final Method lWriteReplace = aJavaObject.getClass().getDeclaredMethod("writeReplace");
                if (lWriteReplace != null)
                {
                    lWriteReplace.setAccessible(true);
                    return aEngine.serialize(lWriteReplace.invoke(aJavaObject), aObjectPool, aClassdefPool);
                }
            }
            catch (NoSuchMethodException e)
            {
                // Do nothing, just continue normal operation.
            }
            catch (Exception e)
            {
                throw new HessianSerializerException(String.format("ObjectHelperBis error while trying to invoke 'writeReplace' on instance of class: '%1$s'.", aJavaObject.getClass().getName()), e);
            }
        }
        ////////////////////////////////////////////////////////////////////////////

        // Get the class of the object we have to handle.
        final Class lJavaClass = aJavaObject.getClass();
        // Fetch the field  information, we need this in several places.
        final List<Field> lJavaFields = getFieldInfo(lJavaClass);
        // Find info about the annotated methods.
        final AnnotatedMethods lAnnotated = getAnnotatedMethods(lJavaClass);

        // Part I : the class.
        // Look for the appropriate classdef. If we cannot find one
        // we have to create one.
        HessianClassdef lDef = aClassdefPool.get(lJavaClass);
        if(lDef == null)
        {
            // We have to create the classdef object first.
            final String lTypeName = aEngine.getNamer().mapJava2Hessian(lJavaClass.getName());
            lDef = new HessianClassdef(new HessianString(lTypeName));
            // Remember the definition in case we encounter other
            // objects of the same class.
            aClassdefPool.put(lJavaClass, lDef);

            // Add normal field names.
            for (Field lFld : lJavaFields)
            {
                lDef.add(new HessianString(lFld.getName()));
            }

            // If there is an annotated hessian serializer method
            // present, we also add the extra fields.
            if(lAnnotated.cons != null)
            {
                for(int i = 0; i < lAnnotated.cons.getParameterTypes().length; i++)
                {
                    lDef.add(new HessianString(HESCONS + i));
                }
            }
        }

        // We add a check on the definition. If there are no fields inside the
        // object, the object class cannot have meaning ...
        if (lDef.size() <= 0)
            throw new HessianSerializerException(String.format("ObjectHelperBis error while serializing. Cannot find any fields to serialize instances of class: '%1$s'.", lJavaClass.getName()));

        // Part II: the object.
        // Create object and write the properties according to the
        // classdef properties.
        final HessianObject lResult = new HessianObject(lDef);
        // Remember the object in case we encounter recursive sub-elements.
        aObjectPool.put(aJavaObject, lResult);

        for (Field lFld : lJavaFields)
        {
            try
            {
                lFld.setAccessible(true);
                final HessianValue lFieldVal = aEngine.serialize(lFld.get(aJavaObject), aObjectPool, aClassdefPool);
                lResult.add(lFieldVal);
            }
            catch (HessianSerializerException e)
            {
                throw new HessianSerializerException(String.format("ObjectHelperBis error while serializing. Error while serializing field: '%1$s' from instance of class: '%2$s'.", lFld.getName(), lJavaClass.getName()), e);
            }
            catch (Exception e)
            {
                throw new HessianSerializerException(String.format("ObjectHelperBis error while serializing. Error while reading field: '%1$s' from instance of class: '%2$s'.", lFld.getName(), lJavaClass.getName()), e);
            }
        }

        // Check if we have an annotated class.
        if (lAnnotated.serialize != null)
        {
            Object[] lVals;
            try
            {
                lVals = (Object[]) lAnnotated.serialize.invoke(aJavaObject);
            }
            catch(Exception e)
            {
                throw new HessianSerializerException(String.format("ObjectHelperBis error while serializing. Error while invoking the @HessianSerialize method called '%1$s(...)' on an instance of class: '%2$s'.", lAnnotated.serialize.getName(), lJavaClass.getName()), e);
            }

            int i = 0;
            try
            {
                for (Object lVal : lVals)
                {
                    final HessianValue lFieldVal = aEngine.serialize(lVal, aObjectPool, aClassdefPool);
                    lResult.add(lFieldVal);
                    i++;
                }
            }
            catch(HessianSerializerException e)
            {
                throw new HessianSerializerException(String.format("ObjectHelperBis error while serializing. Error while serializing element nr %1$d from the @HessianSerialize method: '%2$s(...)' on instance of class: '%3$s'.", i, lAnnotated.serialize.getName(), lJavaClass.getName()), e);
            }
        }
        return lResult;
    }

    public Object deserialize(HessianValue aValue, HessianSerializer aEngine, Map<HessianValue, Object> aPool)
    throws HessianSerializerException
    {
        if (!(aValue instanceof HessianObject))
            throw new HessianSerializerException(String.format("ObjectHelperBis error while deserializing. Expected a HessianObject but received instanceo of: '%1$s'.", aValue.getClass().getName()));

        // Gather some information about the object before proceeding.
        final HessianObject lObj = (HessianObject) aValue;
        final HessianClassdef lDef = lObj.getHessianClassdef();
        final HessianString lType = lDef.getType();

        try
        {
            final String lTypeName = aEngine.getNamer().mapHessian2Java(lType.getValue());
            final Class lJavaClass = Class.forName(lTypeName);
            final List<Field> lJavaFields = getFieldInfo(lJavaClass);

            // Find info about the annotated methods.
            final AnnotatedMethods lAnnotated = getAnnotatedMethods(lJavaClass);

            Object lResult;
            
            if (lAnnotated.cons != null)
            {
                // Let's get the field values to pass to the constructor
                int lCnt = lAnnotated.cons.getParameterTypes().length;
                final Object[] lAttrs = new Object[lCnt];
                for (int i = 0; i < lCnt; i++)
                {
                    final String lFldName = HESCONS + i;
                    final HessianValue lVal = lObj.getField(lFldName);

                    try
                    {
                        lAttrs[i] = aEngine.deserialize(lVal, aPool);
                    }
                    catch(HessianSerializerException e)
                    {
                        throw new HessianSerializerException(String.format("ObjectHelperBis error while deserializing. Error while calling the @HessianConstruct constructor in class: '%1$s' on parameter nr: %2$d with a value of class: '%3$s'.", lJavaClass.getName(), i, lVal.getClass().getName()), e);
                    }
                }

                // Create a new instance using the annotated constructor.
                try
                {
                    lResult = lAnnotated.cons.newInstance(lAttrs);
                }
                catch(Exception e)
                {
                    throw new HessianSerializerException(String.format("ObjectHelperBis error while deserializing. Tried to instantiate an object (using annotated constructor) of class: '%1$s'.", lType.getValue()), e);
                }               
            }
            else
            {
                // Create a new instance using the default constructor
                lResult = lJavaClass.newInstance();
            }

            // Add it to the pool before dealing with the properties because of possible recursion.
            aPool.put(aValue, lResult);

            final Iterator<HessianString> lTypeIter = lDef.iterator();
            int i = 0;
            while (lTypeIter.hasNext())
            {
                final String lHessianFieldName = lTypeIter.next().getValue();
                for (Field lFld : lJavaFields)
                {
                    // Write the property.
                    if (lFld.getName().equals(lHessianFieldName))
                    {
                        final Object lFldValue = aEngine.deserialize(lObj.getField(i), aPool);

                        try
                        {
                            lFld.setAccessible(true);
                            lFld.set(lResult, lFldValue);
                            i++;
                            break;
                        }
                        catch (Exception e)
                        {
                            throw new HessianSerializerException(String.format("ObjectHelperBis error while deserializing. Type error while trying to set the field: '%1$s' in class: '%2$s' with a value of class: '%3$s'.", lFld.getName(), lJavaClass.getName(), lFldValue.getClass().getName()), e);
                        }
                    }
                }
            }

            // First we have to cope with the special case of "readResolve" objects.
            // It is described in the official specs of the serializable interface.
            ////////////////////////////////////////////////////////////////////////////
            if (lResult instanceof Serializable)
            {
                try
                {
                    final Method lReadResolve = lResult.getClass().getDeclaredMethod("readResolve");
                    if (lReadResolve != null)
                    {
                        lReadResolve.setAccessible(true);
                        lResult = lReadResolve.invoke(lResult);
                        // Replace previous instance of the stored reference.
                        aPool.put(aValue, lResult);
                    }
                }
                catch (NoSuchMethodException e)
                {
                    // Do nothing, just continue normal operation.
                }
                catch (Exception e)
                {
                    throw new HessianSerializerException(String.format("ObjectHelperBis error while deserializing. Tried to invoke 'readResolve' on instance of class: '%1$s'.", lResult.getClass().getName()), e);
                }
            }
            ////////////////////////////////////////////////////////////////////////////

            return lResult;
        }
        catch (ClassNotFoundException e)
        {
            throw new HessianSerializerException(String.format("ObjectHelperBis error while deserializing. Tried to load the class: '%1$s'.", lType.getValue()), e);
        }
        catch (IllegalAccessException e)
        {
            throw new HessianSerializerException(String.format("ObjectHelperBis error while deserializing. Tried to access the class: '%1$s'.", lType.getValue()), e);
        }
        catch (InstantiationException e)
        {
            throw new HessianSerializerException(String.format("ObjectHelperBis error while deserializing. Tried to instantiate an object (using default constructor) of class: '%1$s'.", lType.getValue()), e);
        }
    }

    public Class getHelpedClass()
    {
        return helpedClass;
    }
}