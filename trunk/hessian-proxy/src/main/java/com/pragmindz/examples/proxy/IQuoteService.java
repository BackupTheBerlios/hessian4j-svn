package com.pragmindz.examples.proxy;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: Roger
 * Date: 15-mrt-2008
 * Time: 19:17:47
 * To change this template use File | Settings | File Templates.
 */
public interface IQuoteService
{
    BigDecimal getQuote(String aTicker);
}
