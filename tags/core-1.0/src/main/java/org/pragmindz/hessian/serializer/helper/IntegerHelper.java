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
import org.pragmindz.hessian.model.HessianInteger;
import org.pragmindz.hessian.model.HessianValue;
import org.pragmindz.hessian.model.HessianClassdef;
import org.pragmindz.hessian.serializer.HessianSerializer;
import org.pragmindz.hessian.serializer.HessianSerializerException;

import java.util.Map;

/**
 * Integers can be directly represented in Hessian. There is a one to one mapping.
 */
public class IntegerHelper
implements HessianHelper
{
    public HessianValue serialize(Object aJavaObject, HessianSerializer aEngine, Map<Object, HessianValue> aObjectPool, Map<Class, HessianClassdef> aClassdefPool)
    throws HessianSerializerException
    {
        if(!Integer.class.isAssignableFrom(aJavaObject.getClass())) throw new HessianSerializerException(String.format("IntegerHelper error while serializing. Expected a Integer instance, but received an instance of class: '%1$s'.", aJavaObject.getClass()));
        return HessianInteger.valueOf((Integer) aJavaObject);
    }

    public Object deserialize(HessianValue aValue, HessianSerializer aEngine, Map<HessianValue, Object> aPool)
    throws HessianSerializerException
    {
        if(!(aValue instanceof HessianInteger)) throw new HessianSerializerException(String.format("IntegerHelper error while deserializing. Expected a HessianInteger object and expected an instance of class: '%1$s'.", aValue.getClass().getName()));
        else return ((HessianInteger) aValue).getValue();
    }

    public Class getHelpedClass()
    {
        return Integer.class;
    }
}
