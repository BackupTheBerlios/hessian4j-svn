package org.pragmindz.hessian.proxy;
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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import junit.framework.Assert;

public class BasicServiceProviderTest
{

    @Test
    public void testProvider_1() throws IOException, ParserConfigurationException, SAXException, IllegalAccessException, InstantiationException, ClassNotFoundException, ServiceNotFound
    {
        BasicServiceProvider lProv = new BasicServiceProvider();
        Object lServ1 = lProv.getService("testService");
        Assert.assertEquals(TestService.class, lServ1.getClass());
        Object lServ2 = lProv.getService("testService");
        Assert.assertNotSame(lServ1, lServ2);
        Object lServ3 = lProv.getService("testService2");
        Object lServ4 = lProv.getService("testService2");
        Assert.assertSame(lServ3, lServ4);
        try
        {
            lProv.getService("service3");
            Assert.fail();
        }
        catch (ServiceNotFound e)
        {
            //ok
        }
    }

    public static class TestService
    {
    }

    public static class TestService2
    {
    }

}
