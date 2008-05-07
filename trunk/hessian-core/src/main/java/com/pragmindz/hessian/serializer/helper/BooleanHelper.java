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
import com.pragmindz.hessian.model.HessianValue;
import com.pragmindz.hessian.model.HessianBoolean;
import com.pragmindz.hessian.model.HessianClassdef;
import com.pragmindz.hessian.serializer.HessianSerializer;
import com.pragmindz.hessian.serializer.HessianSerializerException;

import java.util.Map;

/**
 * Bools can be directly represented in Hessian. There is a one to one mapping.
 */
public class BooleanHelper
implements HessianHelper
{
    public HessianValue serialize(Object aJavaObject, HessianSerializer aEngine, Map<Object, HessianValue> aObjectPool, Map<Class, HessianClassdef> aClassdefPool)
    throws HessianSerializerException
    {
        if(!Boolean.class.isAssignableFrom(aJavaObject.getClass())) throw new HessianSerializerException(String.format("BooleanHelper error while serializing. Expected an instance of class Boolean, but received an instance of class: '%1$s'.", aJavaObject.getClass().getName()));
        if(Boolean.TRUE.equals(aJavaObject)) return HessianBoolean.TRUE;
        else return HessianBoolean.FALSE;
    }

    public Object deserialize(HessianValue aValue, HessianSerializer aEngine, Map<HessianValue, Object> aPool)
    throws HessianSerializerException
    {
        if(!(aValue instanceof HessianBoolean)) throw new HessianSerializerException(String.format("BooleanHelper error while deserializing. Expected a HessianBoolean intsance but received one from class: '%1$s'.", aValue.getClass().getName()));
        else return ((HessianBoolean) aValue).isValue();
    }

    public Class getHelpedClass()
    {
        return Boolean.class;
    }
}