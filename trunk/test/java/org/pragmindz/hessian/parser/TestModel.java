package org.pragmindz.hessian.parser;
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
import org.junit.Test;
import org.junit.Assert;
import org.pragmindz.hessian.model.HessianInteger;
import org.pragmindz.hessian.model.HessianLong;

public class TestModel
{
    @Test
    public void testIntegers()
    {
        HessianInteger lInt = HessianInteger.valueOf(49);
        Assert.assertEquals(49, lInt.getValue());

        lInt = HessianInteger.valueOf(50);
        Assert.assertEquals(50, lInt.getValue());

        lInt = HessianInteger.valueOf(0);
        Assert.assertEquals(0, lInt.getValue());

        lInt = HessianInteger.valueOf(-49);
        Assert.assertEquals(-49, lInt.getValue());

         lInt = HessianInteger.valueOf(-50);
        Assert.assertEquals(-50, lInt.getValue());
    }

    @Test
    public void testLongs()
    {
        HessianLong lLong = HessianLong.valueOf(49L);
        Assert.assertEquals(49L, lLong.getValue());

        lLong = HessianLong.valueOf(50L);
        Assert.assertEquals(50L, lLong.getValue());

        lLong = HessianLong.valueOf(0L);
        Assert.assertEquals(0L, lLong.getValue());

        lLong = HessianLong.valueOf(-49L);
        Assert.assertEquals(-49L, lLong.getValue());

         lLong = HessianLong.valueOf(-50L);
        Assert.assertEquals(-50L, lLong.getValue());
    }
}
