package com.pragmindz.examples.proxy;

import com.pragmindz.hessian.proxy.IServiceProvider;
import com.pragmindz.hessian.proxy.ServiceNotFound;

public class BasicServiceProvider implements IServiceProvider
{
    public Object getService(String aServiceName) throws ServiceNotFound
    {
        if ("quoteService".equals(aServiceName))
        {
           return new QuoteService();
        }
        else
            throw new ServiceNotFound();
    }
}
