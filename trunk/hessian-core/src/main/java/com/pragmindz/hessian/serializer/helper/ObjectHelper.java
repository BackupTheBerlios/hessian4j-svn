package com.pragmindz.hessian.serializer.helper;/*     Hessian4J - Java Hessian Library     Copyright (C) 2008 PragMindZ     http://www.pragmindz.com     mailto://???      This library is free software; you can redistribute it and/or     modify it under the terms of the GNU Lesser General Public     License as published by the Free Software Foundation; either     version 2.1 of the License, or (at your option) any later version.      This library is distributed in the hope that it will be useful,     but WITHOUT ANY WARRANTY; without even the implied warranty of     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU     Lesser General Public License for more details.      You should have received a copy of the GNU Lesser General Public     License along with this library; if not, write to the Free Software     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA */

import com.pragmindz.hessian.model.*;
import com.pragmindz.hessian.serializer.HessianSerializer;
import com.pragmindz.hessian.serializer.HessianSerializerException;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

/**
 * Use getters/setters to obtain the JavaBean properties to serialize them.
 */
public class ObjectHelper
implements HessianHelper    
{
    public HessianValue serialize(Object aJavaObject, HessianSerializer aEngine, Map<Object, HessianValue> aObjectPool, Map<Class, HessianClassdef> aClassdefPool)
    throws HessianSerializerException
    {                
        final Class lJavaClass = aJavaObject.getClass();

        try
        {
            // Fetch the property information, we need this in several places.
            final PropertyDescriptor[] lPropDesc = Introspector.getBeanInfo(lJavaClass, Introspector.USE_ALL_BEANINFO).getPropertyDescriptors();

            // Part I : the class.
            // Look for the appropriate classdef. If we cannot find one
            // we have to create one.
            HessianClassdef lDef = aClassdefPool.get(lJavaClass);
            if(lDef == null)                        
            {
                // We have to create the classdef object first.
                final String lTypeName = aEngine.getNamer().mapJava2Hessian(lJavaClass.getName());
                final HessianString lType = new HessianString(lTypeName);
                lDef = new HessianClassdef(lType);
                // Remember the definition in case we encounter other
                // objects of the same class.
                aClassdefPool.put(lJavaClass, lDef);

                for (PropertyDescriptor aPropDesc : lPropDesc)
                {
                    final Method lReader = aPropDesc.getReadMethod();
                    final Method lWriter = aPropDesc.getWriteMethod();
                    final String lPropName = aPropDesc.getName();
                    // Only serialize if the property is READ-WRITE.
                    if (lReader != null && lWriter != null) lDef.add(new HessianString(lPropName));
                }
            }

            // We add a check on the definition. If there are no fields inside the
            // object, the object class cannot have meaning ...
            if(lDef.size() <= 0)
                throw new HessianSerializerException(String.format("ObjectHelper error while serializing. Cannot find any properties to serialize instances of class: '%1$s'.", lJavaClass.getName()));

            // Part II: the object.
            // Create object and write the properties according to the
            // classdef properties.
            final HessianObject lResult = new HessianObject(lDef);
            // Remember the object in case we encounter recursive sub-elements.
            aObjectPool.put(aJavaObject, lResult);

            Iterator<HessianString> lTypeIter = lDef.iterator();
            while(lTypeIter.hasNext())
            {
                final HessianString lHessianFieldName =  lTypeIter.next();
                for (PropertyDescriptor lProp : lPropDesc)
                {
                    // Write the property.
                    final String lPropName = lProp.getName();
                    if(lPropName.equals(lHessianFieldName.getValue()))
                    {
                        try
                        {
                            final Method lReader = lProp.getReadMethod();
                            lResult.add(aEngine.serialize(lReader.invoke(aJavaObject), aObjectPool, aClassdefPool));
                        }
                        catch(IllegalAccessException e)
                        {
                            throw new HessianSerializerException(String.format("ObjectHelper error while serializing. Error reading property: '%1$s' from class: '%2$s'.", lPropName, lJavaClass.getName()), e);
                        }
                        catch(InvocationTargetException e)
                        {
                            throw new HessianSerializerException(String.format("ObjectHelper error while serializing. Error reading property: '%1$s' from class: '%2$s'.", lPropName, lJavaClass.getName()), e);
                        }
                    }
                }
            }
            return lResult;
        }
        catch(IntrospectionException e)
        {
            throw new HessianSerializerException(String.format("ObjectHelper error while serializing. Error while constructing HessianClassdef for class: '%1$s'. ",lJavaClass.getName()), e);
        }
    }

    public Object deserialize(HessianValue aValue, HessianSerializer aEngine, Map<HessianValue, Object> aPool)
    throws HessianSerializerException
    {
        if(!(aValue instanceof HessianObject)) throw new HessianSerializerException(String.format("ObjectHelper error while deserializing. Expected a HessianObject but received an instance of class: '%1$s'.", aValue.getClass().getName()));

        // Gather some information about the object before proceeding.
        final HessianObject lObj = (HessianObject) aValue;
        final HessianClassdef lDef = lObj.getHessianClassdef();
        final HessianString lType = lDef.getType();

        try
        {
            final String lTypeName = aEngine.getNamer().mapHessian2Java(lType.getValue());
            final Class lJavaClass = Class.forName(lTypeName);
            final PropertyDescriptor[] lPropDesc = Introspector.getBeanInfo(lJavaClass, Introspector.USE_ALL_BEANINFO).getPropertyDescriptors();

            // Create a new instance.
            final Object lResult = lJavaClass.newInstance();
            // Add it to the pool before dealing with the properties because of possible recursion.
            aPool.put(aValue, lResult);

            Iterator<HessianString> lTypeIter = lDef.iterator();
            int i = 0;
            while(lTypeIter.hasNext())
            {
                final String lHessianFieldName =  lTypeIter.next().getValue();
                for(PropertyDescriptor lProp : lPropDesc)
                {
                    // Write the property.
                    final String lPropName = lProp.getName();
                    if(lPropName.equals(lHessianFieldName))
                    {
                        try
                        {
                            final Object lFieldVal = aEngine.deserialize(lObj.getField(i), aPool);
                            final Method lWriter = lProp.getWriteMethod();
                            lWriter.invoke(lResult, lFieldVal);
                            i++;
                            break;
                        }
                        catch(IllegalAccessException e)
                        {
                            throw new HessianSerializerException(String.format("ObjectHelper error while writing property: '%1$s' from class: '%2$s'.", lPropName, lJavaClass.getName()), e);
                        }
                        catch(InvocationTargetException e)
                        {
                            throw new HessianSerializerException(String.format("ObjectHelper error while writing property: '%1$s' from class: '%2$s'.", lPropName, lJavaClass.getName()), e);
                        }
                    }
                }
            }
            return lResult;
        }
        catch(ClassNotFoundException e)
        {
            throw new HessianSerializerException(String.format("ObjectHelper error while trying to load the class: '%1$s'.", lType.getValue()), e);
        }
        catch(IntrospectionException e)
        {
            throw new HessianSerializerException(String.format("ObjectHelper error while trying to access the properties the class: '%1$s'.", lType.getValue()), e);
        }
        catch(IllegalAccessException e)
        {
            throw new HessianSerializerException(String.format("ObjectHelper error while trying to access the properties the class: '%1$s'.", lType.getValue()), e);
        }
        catch(InstantiationException e)
        {
            throw new HessianSerializerException(String.format("ObjectHelper error error while trying to instantiate an object (using default constructor) of class: '%1$s'.", lType.getValue()), e);
        }
    }

    public Class getHelpedClass()
    {
        return Object.class;
    }
}