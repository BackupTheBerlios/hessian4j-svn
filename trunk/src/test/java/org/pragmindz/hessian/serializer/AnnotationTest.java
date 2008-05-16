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

import java.util.Date;

public class AnnotationTest
{

    @Test
    public void testAnnotion() throws HessianSerializerException
    {
        MyDate lDate = new MyDate(new Date().getTime(),"UTC+01");
        lDate.setTestAttr("oeleboele");
        HessianSerializer serializer = new HessianSerializer();
        HessianObject lVal = (HessianObject)serializer.serialize(lDate);
        Assert.assertEquals(5,lVal.getHessianClassdef().size());
        Assert.assertNotNull(lVal.getField("_HESCONS_0"));
        Assert.assertNotNull(lVal.getField("_HESCONS_1"));
        MyDate lDate2 = (MyDate) serializer.deserialize(lVal);
        Assert.assertEquals(lDate.theDate,lDate2.theDate);
        Assert.assertEquals(lDate.theTimeZone,lDate2.theTimeZone);
        Assert.assertEquals(lDate.testAttr, lDate2.testAttr);
    }


    public static class MyDate
    {
        private Date theDate;
        private String theTimeZone;
        private String testAttr;

        @HessianConstruct
        public MyDate(long aTime, String aTimeZone)
        {
            theDate = new Date(aTime);
            theTimeZone = aTimeZone;
        }

        @HessianSerialize
        public Object[] getTime()
        {
            return new Object[] {theDate.getTime(), theTimeZone};
        }

        public void setTestAttr(final String aTestAttr)
        {
            testAttr = aTestAttr;
        }
    }
}
