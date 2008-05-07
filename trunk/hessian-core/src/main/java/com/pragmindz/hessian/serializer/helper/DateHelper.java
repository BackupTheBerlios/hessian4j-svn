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
import com.pragmindz.hessian.model.HessianDate;
import com.pragmindz.hessian.model.HessianValue;
import com.pragmindz.hessian.model.HessianClassdef;
import com.pragmindz.hessian.serializer.HessianSerializer;
import com.pragmindz.hessian.serializer.HessianSerializerException;

import java.util.Date;
import java.util.Map;

public class DateHelper
implements HessianHelper
{
    public HessianValue serialize(Object aJavaObject, HessianSerializer aEngine, Map<Object, HessianValue> aObjectPool, Map<Class, HessianClassdef> aClassdefPool)
    throws HessianSerializerException
    {
        if(!Date.class.isAssignableFrom(aJavaObject.getClass())) throw new HessianSerializerException(String.format("DateHelper error while serializing. Expected an instance of class Date but received one of class: '%1$s'.", aJavaObject.getClass().getName()));
        return new HessianDate((Date) aJavaObject);
    }

    public Object deserialize(HessianValue aValue, HessianSerializer aEngine, Map<HessianValue, Object> aPool)
    throws HessianSerializerException
    {
        if(!(aValue instanceof HessianDate)) throw new HessianSerializerException(String.format("DateHelper error while deserializing. Expected an instance of class HessianDate but received one of class: '%1$s'.", aValue.getClass().getName()));
        else return ((HessianDate)aValue).getValue();
    }

    public Class getHelpedClass()
    {
        return Date.class;
    }
}