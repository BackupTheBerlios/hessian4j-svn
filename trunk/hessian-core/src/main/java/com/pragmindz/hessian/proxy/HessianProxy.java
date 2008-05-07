package com.pragmindz.hessian.proxy;

import com.pragmindz.hessian.model.*;
import com.pragmindz.hessian.serializer.HessianSerializer;
import com.pragmindz.hessian.parser.HessianParser;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

public class HessianProxy implements InvocationHandler
{
    final private URL url;
    private static HessianSerializer serializer = new HessianSerializer();

    public HessianProxy(String anUrl) throws MalformedURLException
    {
        url = new URL(anUrl);
    }

    /**
     * Invokes an HTTP-post request with a Hessian rpc call as payload.
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        HttpURLConnection lConn = (HttpURLConnection) url.openConnection();
        lConn.setRequestMethod("POST");
        lConn.setDoOutput(true);
        final OutputStream lOutStr = lConn.getOutputStream();

        List<HessianValue> lArgs = new ArrayList<HessianValue>(args.length);
        for (Object lArg : args)
        {
            lArgs.add(serializer.serialize(lArg));
        }

        HessianCall lCall = new HessianCall(method.getName(), lArgs);

        lCall.render(lOutStr, new ArrayList<HessianString>(), new ArrayList<HessianClassdef>(), new ArrayList<HessianComplex>());
        lOutStr.flush();

        final int lCode = lConn.getResponseCode();
        if (lCode != 200)
        {
            //TODO add error handling
            throw new Exception("Received http error response : " + lCode);
        }
        else
        {
            //Read the reply
            InputStream lInStr = lConn.getInputStream();
            HessianParser lParser = new HessianParser(lInStr);
            HessianSuccessReply lReply = (HessianSuccessReply) lParser.nextValue();
            return serializer.deserialize(lReply.getValue());
        }
    }

}
