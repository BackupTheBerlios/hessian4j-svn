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
import org.pragmindz.hessian.model.HessianObject;
import junit.framework.Assert;
import org.junit.Test;

import java.io.Serializable;

public class ResolverTest
{

    public static class MyClass1 implements Serializable
    {
        String firstName;
        String name;

        public MyClass1(String aFirstName, String aName)
        {
            name = aName;
            firstName = aFirstName;
        }

        public Object writeReplace()
        {
            MyClass2 lPojo = new MyClass2();
            lPojo.name = firstName+","+name;
            return lPojo;
        }
    }

    public static class MyClass2 implements Serializable
    {
       String name;

        public Object readResolve()
        {
            String lFirstName = name.substring(0,name.indexOf(','));
            String lLastName = name.substring(name.indexOf(',')+1,name.length());
            return new MyClass1(lFirstName,lLastName);
        }
    }

    @Test
    public void testWriteReplace() throws HessianSerializerException
    {
        MyClass1 lPojo = new MyClass1("Boris","Jeltsjin");
        HessianSerializer serializer = new HessianSerializer();
        HessianObject lVal = (HessianObject)serializer.serialize(lPojo);
        Assert.assertEquals("org.pragmindz.hessian.serializer.ResolverTest$MyClass2",lVal.getHessianClassdef().getType().getValue());

        MyClass1 lPojo2 = (MyClass1) serializer.deserialize(lVal);
        Assert.assertEquals("Boris",lPojo2.firstName);
        Assert.assertEquals("Jeltsjin",lPojo2.name);
    }
}
