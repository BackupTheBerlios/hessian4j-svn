package com.pragmindz.examples.proxy;

import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.Server;
import com.pragmindz.hessian.proxy.HessianServlet;

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
