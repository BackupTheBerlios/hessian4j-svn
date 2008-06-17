package org.pragmindz.hessian.serializer.helper;
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
import org.pragmindz.hessian.model.HessianValue;
import org.pragmindz.hessian.model.HessianClassdef;
import org.pragmindz.hessian.model.HessianObject;
import org.pragmindz.hessian.model.HessianString;
import org.pragmindz.hessian.serializer.HessianSerializer;
import org.pragmindz.hessian.serializer.HessianSerializerException;

import java.util.Map;

public class EnumHelper
implements HessianHelper
{
    public Object deserialize(HessianValue aValue, HessianSerializer aEngine, Map<HessianValue, Object> aPool)
    throws HessianSerializerException
    {
        if(!(aValue instanceof HessianObject)) throw new HessianSerializerException(String.format("EnumHelper error while deserializing. Expected a HessianObject value, but received a '%1$s' value.", aValue.getClass().getName()));
        final HessianObject lObj = (HessianObject) aValue;
        final HessianClassdef lDef = lObj.getHessianClassdef();
        if(lDef == null || lDef.getType() == null || lDef.getType().getValue() == null) throw new HessianSerializerException("EnumHelper error while deserializing. Expected a HessianObject values with a valid Classdef.");
        final String lType = lDef.getType().getValue();

        final HessianValue lVal = lObj.getField(0);
        if(!(lVal instanceof HessianString)) throw new HessianSerializerException(String.format("EnumHelper error while deserializing. Expected a HessianString representation of the value but encountered a: '%1$s' value.", lVal.getClass().getName()));
        final HessianString lStrVal = (HessianString) lVal;
        if(lStrVal.getValue() == null) throw new HessianSerializerException(String.format("EnumHelper error while deserializing. Encountered an emtpy string content that cannot represent an enum constant."));
        final String lEnumRepr = lStrVal.getValue();

        final Class lEnumClass;
        try
        {
            lEnumClass = Class.forName(lType);
        }
        catch (ClassNotFoundException e)
        {
            throw new HessianSerializerException(String.format("EnumHelper error while deserializing. Failed loading the enum class: '%1$s'.", lType), e);
        }

        if(lEnumClass.isEnum())
        {
            final Object[] lEnumVals = lEnumClass.getEnumConstants();
            for(Object lEnumVal: lEnumVals) if(lEnumVal.toString().equals(lEnumRepr)) return lEnumVal;
        }
        else
        {
            throw new HessianSerializerException(String.format("EnumHelper error while deserializing. Tried to handle a non-enum class: '%1$s'.", lType));
        }

        throw new HessianSerializerException(String.format("EnumHelper error while deserializing. The enum class: '%1$s' was found, but it does not contain the requested constant: '%2$s'.", lType, lEnumRepr));
    }

    public HessianValue serialize(Object aJavaObject, HessianSerializer aEngine, Map<Object, HessianValue> aObjectPool, Map<Class, HessianClassdef> aClassdefPool)
    throws HessianSerializerException
    {
        final Class lJavaClass = aJavaObject.getClass();
        if(!(aJavaObject instanceof Enum)) throw new HessianSerializerException(String.format("EnumHelper error while serializing. Tried to handle a non-enum class: '%1$s'.", lJavaClass.getName()));

        // Create a new class if needed.
        HessianClassdef lDef = aClassdefPool.get(lJavaClass);
        if(lDef == null)        
        {
            lDef = new HessianClassdef(lJavaClass.getName(), "constant");
            aClassdefPool.put(lJavaClass, lDef);            
        }

        // Create an object.
        HessianObject lObj = new HessianObject(lDef);
        lObj.add(new HessianString(aJavaObject.toString()));
        return lObj;
    }

    public Class getHelpedClass()
    {
        return Enum.class;
    }
}
