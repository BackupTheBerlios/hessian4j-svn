package com.pragmindz.examples.proxy;

import com.pragmindz.hessian.proxy.ProxyFactory;
import com.pragmindz.hessian.proxy.HessianProxy;

import java.math.BigDecimal;
import java.net.MalformedURLException;

public class QuoteClient
{
    public static void main(String[] args) throws MalformedURLException
    {
        ProxyFactory lFact = new ProxyFactory();
        IQuoteService lProxy = (IQuoteService) lFact.create("http://localhost:8090/quoteService",IQuoteService.class);
        System.out.println("Quote for IBM : "+lProxy.getQuote("IBM"));
    }
}
