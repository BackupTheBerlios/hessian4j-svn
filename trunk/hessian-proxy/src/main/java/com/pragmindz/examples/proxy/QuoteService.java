package com.pragmindz.examples.proxy;

import java.math.BigDecimal;

public class QuoteService implements IQuoteService
{
    public BigDecimal getQuote(String aTicker)
    {
        return new BigDecimal("10.00");
    }
}
