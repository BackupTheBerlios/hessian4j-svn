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
import com.pragmindz.hessian.model.HessianDouble;
import com.pragmindz.hessian.model.HessianValue;
import com.pragmindz.hessian.model.HessianClassdef;
import com.pragmindz.hessian.serializer.HessianSerializer;
import com.pragmindz.hessian.serializer.HessianSerializerException;

import java.util.Map;

/**
 * Doubles can be directly represented in Hessian. There is a one to one mapping.
 */
public class DoubleHelper
implements HessianHelper
{
    public HessianValue serialize(Object aJavaObject, HessianSerializer aEngine, Map<Object, HessianValue> aObjectPool, Map<Class, HessianClassdef> aClassdefPool)
    throws HessianSerializerException
    {
        if (!Double.class.isAssignableFrom(aJavaObject.getClass())) throw new HessianSerializerException(String.format("DoubleHelper error while serializing. Expected a Double instance, but reveived an instance of class: '%1$s'.", aJavaObject.getClass().getName()));
        return new HessianDouble((Double) aJavaObject);
    }

    public Object deserialize(HessianValue aValue, HessianSerializer aEngine, Map<HessianValue, Object> aPool)
    throws HessianSerializerException
    {
        if(!(aValue instanceof HessianDouble)) throw new HessianSerializerException(String.format("DoubleHelper error while deserializing. Expected a HessianDouble, but reveived an instance of class: '%1$s'.", aValue.getClass().getName()));
        else return ((HessianDouble) aValue).getValue();
    }

    public Class getHelpedClass()
    {
        return Double.class;
    }
}
