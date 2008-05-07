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
import com.pragmindz.hessian.model.HessianMap;
import com.pragmindz.hessian.model.HessianString;
import com.pragmindz.hessian.model.HessianValue;
import com.pragmindz.hessian.model.HessianClassdef;
import com.pragmindz.hessian.serializer.HessianSerializer;
import com.pragmindz.hessian.serializer.HessianSerializerException;

import java.util.Map;
import java.util.HashMap;

public class MapHelper
implements HessianHelper
{
    public HessianValue serialize(Object aJavaObject, HessianSerializer aEngine, Map<Object, HessianValue> aObjectPool, Map<Class, HessianClassdef> aClassdefPool)
    throws HessianSerializerException
    {
        if(!Map.class.isAssignableFrom(aJavaObject.getClass())) throw new HessianSerializerException(String.format("MapHelper error while serializing. Expected a Map but received a: '%1$s'.", aJavaObject.getClass().getName()));
        // Create the new map.
        final String lTypeName = aEngine.getNamer().mapJava2Hessian(aJavaObject.getClass().getName());
        final HessianString lType = new HessianString(lTypeName);
        final HessianMap lResult = new HessianMap(lType);

        // Before processing the elements of the list we have to register the map in  the pool.
        // It has to be done before the elements because we have to handle recursive structures.
        aObjectPool.put(aJavaObject, lResult);
        // Now we can start the recursion.
        final Map lMap = (Map) aJavaObject;
        try
        {
            for(Object lKey : lMap.keySet())
            {
                lResult.put(aEngine.serialize(lKey, aObjectPool, aClassdefPool), aEngine.serialize(lMap.get(lKey), aObjectPool, aClassdefPool));
            }
        }
        catch(HessianSerializerException e)
        {
            // Augment the error.
            throw new HessianSerializerException(String.format("MapHelper error while serializing. Trying to serialize a key or value for a map of type: '%1$s'.", lType), e);
        }
        return lResult;
    }

    @SuppressWarnings({"unchecked"})
    public Object deserialize(HessianValue aValue, HessianSerializer aEngine, Map<HessianValue, Object> aPool)
    throws HessianSerializerException
    {
        if(!(aValue instanceof HessianMap)) throw new HessianSerializerException(String.format("MapHelper error while deserializing. Expected a HessianMap but received a: '%1$s'.", aValue.getClass().getName()));
        final HessianMap lMap = (HessianMap) aValue;
        String lType = "<unknown>";
        int lCount = 0;

        try
        {
            Class lMapClass =  HashMap.class;
            if(lMap.getType() != null)
            {
                lType = aEngine.getNamer().mapHessian2Java(lMap.getType().getValue());
                lMapClass = Class.forName(lType);
            }

            // Create a new instance.
            final Map lResult = (Map) lMapClass.newInstance();
            // Add it to the pool before dealing with subelements for recursion.
            aPool.put(aValue, lResult);
            for(java.util.Iterator<HessianValue> it = lMap.keys(); it.hasNext();)
            {
                final HessianValue lKey = it.next();
                final HessianValue lVal = lMap.get(lKey);
                
                lResult.put(aEngine.deserialize(lKey, aPool), aEngine.deserialize(lVal, aPool));
                lCount++;
            }
            return lResult;
        }
        catch(ClassNotFoundException e)
        {
            throw new HessianSerializerException(String.format("MapHelper exception while deserializing. Encountered a map with a type that is not available: '%1$s'.", lType), e);
        }
        catch(IllegalAccessException e)
        {
            throw new HessianSerializerException(String.format("MapHelper error while deserializing.Could not create an instance of: '%1$s'.", lType), e);
        }
        catch(InstantiationException e)
        {
            throw new HessianSerializerException(String.format("MapHelper error while deserializing.Could not create an instance of: '%1$s'.", lType), e);
        }
        catch(HessianSerializerException e)
        {
            throw new HessianSerializerException(String.format("MapHelper error while deserializing an element with index: '%1$d' of a map of type: '%2$s'.", lCount, lType), e);
        }
    }

    public Class getHelpedClass()
    {
        return Map.class;
    }
}