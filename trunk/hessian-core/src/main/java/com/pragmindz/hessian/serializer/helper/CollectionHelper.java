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
import com.pragmindz.hessian.model.HessianList;
import com.pragmindz.hessian.model.HessianString;
import com.pragmindz.hessian.model.HessianValue;
import com.pragmindz.hessian.model.HessianClassdef;
import com.pragmindz.hessian.serializer.HessianSerializer;
import com.pragmindz.hessian.serializer.HessianSerializerException;

import java.util.Collection;
import java.util.Map;
import java.util.LinkedList;

public class CollectionHelper
implements HessianHelper
{
    public HessianValue serialize(Object aJavaObject, HessianSerializer aEngine, Map<Object, HessianValue> aObjectPool, Map<Class, HessianClassdef> aClassdefPool)
    throws HessianSerializerException
    {
        if(!Collection.class.isAssignableFrom(aJavaObject.getClass())) throw new HessianSerializerException(String.format("CollectionHelper error while serializing. Expected a Collection but received an instance of class: '%1$s'.", aJavaObject.getClass().getName()));
        // Create the new list.
        final HessianString lType = new HessianString(aEngine.getNamer().mapJava2Hessian(aJavaObject.getClass().getName()));
        final HessianList lResult = new HessianList(lType);

        // Before processing the elements of the list we have to register the list in  the pool.
        // It has to be done before the elements because we have to handle recursive structures.
        aObjectPool.put(aJavaObject, lResult);
        // Now we can start the recursion.
        final Collection lCol = (Collection) aJavaObject;
        int i = 0;
        try
        {
            for(Object lEl : lCol)
            {
                lResult.add(aEngine.serialize(lEl, aObjectPool, aClassdefPool));
                i++;
            }
        }
        catch(HessianSerializerException e)
        {
            // Augment the exception with more info.
            throw new HessianSerializerException(String.format("CollectionHelper error while serializing element with index %1$d in a collection of type: '%2$s'.", i, lType), e);
        }
        return lResult;        
    }

    @SuppressWarnings({"unchecked"})
    public Object deserialize(HessianValue aValue, HessianSerializer aEngine, Map<HessianValue, Object> aPool)
    throws HessianSerializerException
    {
        if(!(aValue instanceof HessianList)) throw new HessianSerializerException(String.format("CollectionHelper error while deserializing. Expected a HessianList instance but received an instance of class: '%1$s'.", aValue.getClass().getName()));
        final HessianList lList = (HessianList) aValue;
        String lType = "<unknown>";

        try
        {
            Class lCollectionClass =  LinkedList.class;
            if(lList.getType() != null)
            {
                lType = lList.getType().getValue();
                lCollectionClass = Class.forName(lType);
            }

            // Create a new instance.
            final Collection lResult = (Collection) lCollectionClass.newInstance();
            // Add it to the pool. We must do it before the subelements to cope with recursion.
            aPool.put(aValue, lResult);
            // Load the subelements.
            int lCount = 0;
            try
            {
                for(java.util.Iterator<HessianValue> it = lList.iterator(); it.hasNext();)
                {
                    final HessianValue lVal = it.next();
                    lResult.add(aEngine.deserialize(lVal, aPool));
                    lCount++;
                }
            }
            catch(HessianSerializerException e)
            {
                // Augment the exception with more info.
                throw new HessianSerializerException(String.format("CollectionHelper error while deserializing an element with index: '%1$d' of a collection of type: '%2$s'.", lCount, lType), e);
            }
            return lResult;
        }
        catch(ClassNotFoundException e)
        {
            throw new HessianSerializerException(String.format("CollectionHelper error while deserializing. Encountered a list with a type that is not available: '%1$s'.", lType), e);
        }
        catch(IllegalAccessException e)
        {
            throw new HessianSerializerException(String.format("CollectionHelper error while deserializing. Could not create an instance of: '%1$s'.", lType), e);
        }
        catch(InstantiationException e)
        {
            throw new HessianSerializerException(String.format("CollectionHelper error while deserializing. Could not create an instance of: '%1$s'.", lType), e);
        }
    }

    public Class getHelpedClass()
    {
        return Collection.class;
    }
}
