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

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.pragmindz.hessian.proxy.HessianServlet;

public class QuoteServer
{
    public QuoteServer() throws Exception
    {
        Server server = new Server(8090);
        Context root = new Context(server, "/", Context.SESSIONS);
        root.addServlet(new ServletHolder(new HessianServlet(new BasicServiceProvider())), "/*");
        server.start();
    }

    public static void main(String[] args) throws Exception
    {
        QuoteServer lSvr = new QuoteServer();
    }
}