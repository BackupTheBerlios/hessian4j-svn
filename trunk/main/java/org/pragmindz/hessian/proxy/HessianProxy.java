package org.pragmindz.hessian.proxy;
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
import org.pragmindz.hessian.model.*;
import org.pragmindz.hessian.serializer.HessianSerializer;
import org.pragmindz.hessian.parser.HessianParser;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.io.OutputStream;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;

public class HessianProxy implements InvocationHandler
{
    final private URL url;
    private static HessianSerializer serializer = new HessianSerializer();

    public HessianProxy(String anUrl) throws MalformedURLException
    {
        url = new URL(anUrl);
    }

    /**
     * Creates a proxy for the given interface and url.
     *
     * @param anUrl
     * @param anInterface
     * @return
     */
    public static Object create(String anUrl, Class anInterface) throws MalformedURLException
    {
        final HessianProxy lProxy = new HessianProxy(anUrl);
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{anInterface}, lProxy);
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
        //Create the http-connection
        HttpURLConnection lConn = (HttpURLConnection) url.openConnection();
        lConn.setRequestMethod("POST");
        lConn.setDoOutput(true);

        //Create the Hessian call as the payload
        List<HessianValue> lArgs = new ArrayList<HessianValue>(args.length);
        for (Object lArg : args)
        {
            lArgs.add(serializer.serialize(lArg));
        }
        final HessianCall lCall = new HessianCall(method.getName(), lArgs);

        //Render the call to the http output stream.
        final OutputStream lOutStr = lConn.getOutputStream();
        lCall.render(lOutStr, new ArrayList<HessianString>(), new ArrayList<HessianClassdef>(), new ArrayList<HessianComplex>());
        lOutStr.flush();

        //Act upon the http-response
        final int lCode = lConn.getResponseCode();
        if (lCode != 200)
        {
            throw new HessianProxyException("ResponseCode : [" + lConn.getResponseCode() + "] ResponseMsg : [" + lConn.getResponseMessage() + "]");
        }
        else
        {
            //Read the reply
            final HessianParser lParser = new HessianParser(lConn.getInputStream());
            final HessianReply lReply = (HessianReply) lParser.nextValue();
            if (lReply instanceof HessianSuccessReply)
                //Everything went ok, return the deserialized java object
                return serializer.deserialize(((HessianSuccessReply) lReply).getValue());
            else
            {
                //We have fault reply, let's check the reason based on the code contained in the reply.
                HessianFault lFault = ((HessianFaultReply) lReply).getFault();
                switch (lFault.getFaultCode())
                {
                    case NoSuchMethodException:
                        //The invoked method does not exist.
                        throw new NoSuchMethodException(lFault.getMessage().getValue());
                    case NoSuchObjectException:
                        //The invoked service does not exist.
                        throw new NoSuchObjectException(lFault.getMessage().getValue());
                    case ProtocolException:
                        //Syntactic error
                        throw new ProtocolException(lFault.getMessage().getValue());
                    case RequireHeaderException:
                        //A required header was missing
                        throw new MissingHeaderException(lFault.getMessage().getValue());
                    case ServiceException:
                        //The invocation returned an exception, let's propagate it to the caller.
                        throw (Throwable) serializer.deserialize(lFault.getException());
                    default:
                        //Invalid fault code
                        throw new HessianProxyException("Received invalid fault code [" + lFault.getFaultCode() + "]");
                }
            }
        }
    }
}

