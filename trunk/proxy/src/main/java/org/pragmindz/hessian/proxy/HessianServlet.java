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
import org.pragmindz.hessian.parser.HessianParser;
import org.pragmindz.hessian.parser.HessianParserException;
import org.pragmindz.hessian.parser.HessianRenderException;
import org.pragmindz.hessian.serializer.HessianSerializer;
import org.pragmindz.hessian.serializer.HessianSerializerException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.*;

public class HessianServlet extends HttpServlet
{
    private HessianParser parser;
    private HessianSerializer serializer;

    private IServiceProvider serviceProvider;

    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        
        String lProvider = getServletConfig().getInitParameter("serviceProvider");
        if (lProvider == null)
           throw new UnavailableException("No service-provider defined. Define serviceProvider init-parameter.");

        try
        {
            Class lClass = Class.forName(lProvider);
            serviceProvider = (IServiceProvider) lClass.newInstance();
        }
        catch (ClassNotFoundException e)
        {
            throw new UnavailableException("Service-provider class not found ["+lProvider+"]");
        }
        catch (IllegalAccessException e)
        {
            throw new UnavailableException("Unable to instantiate service provider. cause : "+e.getMessage());
        }
        catch (InstantiationException e)
        {
            throw new UnavailableException("Unable to instantiate service provider. cause : "+e.getMessage());
        }
    }

    public void doPost(HttpServletRequest aReq, HttpServletResponse aResp) throws ServletException, IOException
    {
        InputStream lInstr = aReq.getInputStream();
        parser = new HessianParser(lInstr);
        serializer = new HessianSerializer();

        String lSrvName = null;
        String lMethodName = null;
        HessianReply lReply;
        try
        {
            try
            {
                //Retrieve the service from our service provider
                System.out.println("Path : " + aReq.getPathInfo());
                lSrvName = aReq.getPathInfo().substring(1);
                Object lService = serviceProvider.getService(lSrvName);

                //Parse our call
                HessianCall lCall = (HessianCall) parser.nextValue();
                System.out.println("Got : " + lCall.prettyPrint());

                //Deserialize the method arguments
                Class[] lArgTypes = new Class[lCall.getArguments().size()];
                Object[] lArgs = new Object[lCall.getArguments().size()];
                for (int i = 0; i < lCall.getArguments().size(); i++)
                {
                    HessianValue lArg = lCall.getArgument(i);
                    Object lObj = serializer.deserialize(lArg);
                    lArgTypes[i] = lObj.getClass();
                    lArgs[i] = lObj;
                }

                //Unmangle the method name, we don't use the mangling in this implementation
                lMethodName = lCall.getMethod().substring(0, lCall.getMethod().indexOf('_'));

                //Find and invoke the service method.
                Class lClass = lService.getClass();
                Method lMethod = lClass.getMethod(lMethodName, lArgTypes);
                Object lResp = lMethod.invoke(lService, lArgs);

                //Compose the reply
                aResp.setStatus(200);
                HessianValue lVal = serializer.serialize(lResp);
                lReply = new HessianSuccessReply(lVal);
            }
            catch (ServiceNotFound e)
            {
                //Return serviceException
                lReply = new HessianFaultReply(new HessianFault(HessianFault.FaultCode.NoSuchObjectException, new HessianString("Resource with name [" + lSrvName + "] not found.")));
            }
            catch (HessianParserException e)
            {
                //Return protocolException
                lReply = new HessianFaultReply(new HessianFault(HessianFault.FaultCode.ProtocolException, new HessianString(e.getMessage())));
            }
            catch (HessianSerializerException e)
            {
                //Return protocolException
                lReply = new HessianFaultReply(new HessianFault(HessianFault.FaultCode.ProtocolException, new HessianString(e.getMessage())));
            }
            catch (java.lang.NoSuchMethodException e)
            {
                lReply = new HessianFaultReply(new HessianFault(HessianFault.FaultCode.NoSuchMethodException, new HessianString("Method with name [" + lMethodName + "] not found in resource with name [" + lSrvName + "]")));
            }
            catch (Throwable e)
            {
                if (e instanceof InvocationTargetException)
                    e = (((InvocationTargetException) e).getTargetException());
                lReply = new HessianFaultReply(new HessianFault(HessianFault.FaultCode.ServiceException, new HessianString(e.getMessage()), (HessianObject) serializer.serialize(e)));
            }

            //Render and flush the response
            lReply.render(aResp.getOutputStream());
        }
        catch (HessianRenderException e)
        {
            lReply = new HessianFaultReply(new HessianFault(HessianFault.FaultCode.ProtocolException, new HessianString(e.getMessage())));
            try
            {
                lReply.render(aResp.getOutputStream());
            }
            catch (HessianRenderException e1)
            {
                //no-op
            }
        }
        catch (HessianSerializerException e)
        {
            lReply = new HessianFaultReply(new HessianFault(HessianFault.FaultCode.ProtocolException, new HessianString(e.getMessage())));
            try
            {
                lReply.render(aResp.getOutputStream());
            }
            catch (HessianRenderException e1)
            {
                //no-op
            }
        }
        aResp.flushBuffer();
    }
}
