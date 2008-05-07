package com.pragmindz.hessian.proxy;

public interface IServiceProvider
{
    public Object getService(String aServiceName) throws ServiceNotFound;
}
