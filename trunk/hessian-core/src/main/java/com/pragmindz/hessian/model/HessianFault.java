package com.pragmindz.hessian.model;
/*
    Hessian4J - Java Hessian Library
    Copyright (C) 2008 PragMindZ
    http://www.pragmindz.com
    mailto://???

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
import com.pragmindz.hessian.parser.HessianRenderException;

import java.io.OutputStream;
import java.io.IOException;
import java.util.List;


/**
Models a Hessian fault as returned from a call.<br>
Each fault has a number of informative fields, expressed like <map> entries.<br>
The defined fields are code, message, and optionally detail.
 <ul>
 <li>code is one of a short list of strings defined as the <code>FaultCode</code> enumeration.<br>
 <li>message is a user-readable message.<br>
 <li>detail is an object representing the exception.
 </ul>
 <code><pre>
 Grammar :
  fault     ::= 'f' (value value)* 'z'
 </pre></code>
 */
public class HessianFault extends HessianSimple
{
    private final FaultCode faultCode;
    private final HessianString message;
    private final HessianObject exception;

    public enum FaultCode
    {
        ProtocolException, NoSuchObjectException, NoSuchMethodException, RequireHeaderException, ServiceException
    }

    public HessianFault(FaultCode aFaultCode, HessianString aMessage)
    {
        this(aFaultCode, aMessage, null);
    }

    public HessianFault(FaultCode aFaultCode, HessianString aMessage, HessianObject anException)
    {
        faultCode = aFaultCode;
        message = aMessage;
        exception = anException;
    }

    public FaultCode getFaultCode()
    {
        return faultCode;
    }

    public HessianString getMessage()
    {
        return message;
    }

    public HessianObject getException()
    {
        return exception;
    }

    /**
     * Renders a Hessian fault.
     *
     * @see HessianFault
     * @param aStream
     * @param types
     * @param classDefs
     * @param objects
     * @throws HessianRenderException
     */
    public void render(OutputStream aStream, List<HessianString> types, List<HessianClassdef> classDefs, List<HessianComplex> objects)
    throws HessianRenderException
    {
        try
        {
            aStream.write('f');
            new HessianString("code").render(aStream, types, classDefs, objects);
            new HessianString(faultCode.toString()).render(aStream, types, classDefs, objects);
            new HessianString("message").render(aStream, types, classDefs, objects);
            message.render(aStream, types, classDefs, objects);
            if (exception != null)
            {
                new HessianString("detail").render(aStream, types, classDefs, objects);
                exception.render(aStream, types, classDefs, objects);
            }
            aStream.write('z');
        }
        catch (IOException e)
        {
            throw new HessianRenderException(e);
        }
    }

    protected String prettyPrint(String aIndent, List<HessianClassdef> aClassdefs, List<HessianComplex> aObjects)
    {
        final StringBuilder lBuilder = new StringBuilder();
        lBuilder.append(aIndent);
        lBuilder.append("<fault-reply>"); 
        return lBuilder.toString();
    }
}
