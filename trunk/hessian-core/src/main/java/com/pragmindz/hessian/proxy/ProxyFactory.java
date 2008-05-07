package com.pragmindz.hessian.proxy;

import java.lang.reflect.Proxy;
import java.net.MalformedURLException;

public class ProxyFactory
{
    /**
     * Creates a proxy for the given interface and url.
     *
     * @param anUrl
     * @param anInterface
     * @return
     */
    public Object create(String anUrl, Class anInterface) throws MalformedURLException
    {
       final HessianProxy lProxy = new HessianProxy(anUrl);
       return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] {anInterface}, lProxy);
    }
}
