package com.pragmindz.hessian.proxy;

import com.pragmindz.hessian.model.HessianCall;
import com.pragmindz.hessian.model.HessianReply;
import com.pragmindz.hessian.model.HessianSuccessReply;
import com.pragmindz.hessian.model.HessianValue;
import com.pragmindz.hessian.parser.HessianParser;
import com.pragmindz.hessian.parser.HessianParserException;
import com.pragmindz.hessian.parser.HessianRenderException;
import com.pragmindz.hessian.serializer.HessianSerializer;
import com.pragmindz.hessian.serializer.HessianSerializerException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HessianServlet extends HttpServlet
{
    private HessianParser parser;
    private HessianSerializer serializer;

    private final IServiceProvider serviceProvider;

    public HessianServlet(IServiceProvider aServiceProvider)
    {
        serviceProvider = aServiceProvider;
    }
    
    public void init(ServletConfig config) throws ServletException
    {
        //Instantiate our parser
    }

    public void doPost(HttpServletRequest aReq, HttpServletResponse aResp) throws ServletException, IOException
    {
        InputStream lInstr = aReq.getInputStream();
        parser = new HessianParser(lInstr);
        serializer = new HessianSerializer();

        try
        {
            //Retrieve the service from our service provider
            System.out.println("Path : "+aReq.getPathInfo());
            String lSrvName = aReq.getPathInfo().substring(1);
            Object lService = serviceProvider.getService(lSrvName);

            //Parse our call
            HessianCall lCall = (HessianCall) parser.nextValue();
            System.out.println("Got : "+lCall.prettyPrint());

            //Deserialize the method arguments
            Class[] lArgTypes = new Class[lCall.getArguments().size()];
            Object[] lArgs = new Object[lCall.getArguments().size()];
            for (int i=0; i<lCall.getArguments().size(); i++)
            {
                HessianValue lArg = lCall.getArgument(i);
                Object lObj = serializer.deserialize(lArg);
                lArgTypes[i] = lObj.getClass();
                lArgs[i] = lObj;
            }

            //Unmangle the method name, we don't use the mangling in this implementation
            String lName = lCall.getMethod().substring(0,lCall.getMethod().indexOf('_'));

            //Find and invoke the service method.
            Class lClass = lService.getClass();
            Method lMethod = lClass.getMethod(lName,lArgTypes);
            Object lResp = lMethod.invoke(lService,lArgs);

            if (lResp != null)
            {
                //Compose the reply
                aResp.setStatus(200);
                HessianValue lVal = serializer.serialize(lResp);
                HessianReply lReply = new HessianSuccessReply(lVal);
                lReply.render(aResp.getOutputStream());
                aResp.flushBuffer();
            }
        }
        catch (ServiceNotFound e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch (HessianParserException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch (HessianSerializerException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch (HessianRenderException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
