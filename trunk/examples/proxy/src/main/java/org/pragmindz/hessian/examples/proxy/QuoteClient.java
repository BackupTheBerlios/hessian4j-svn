package org.pragmindz.hessian.examples.proxy;
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
import org.pragmindz.hessian.proxy.HessianProxy;
import org.pragmindz.hessian.examples.proxy.IQuoteService;

import java.net.MalformedURLException;
import java.util.Date;

public class QuoteClient
{
    public static void main(String[] args) throws MalformedURLException
    {
        IQuoteService lProxy = (IQuoteService) HessianProxy.create("http://localhost:8080/proxy/quoteService", IQuoteService.class);
        System.out.println("Quote for IBM : "+lProxy.getQuote("IBM"));
        System.out.println("Quote for IBM on specific date : "+lProxy.getQuote("IBM", new Date()));
    }
}
