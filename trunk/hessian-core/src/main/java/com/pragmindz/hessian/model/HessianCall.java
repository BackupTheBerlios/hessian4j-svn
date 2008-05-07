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
import java.util.LinkedList;
import java.util.Iterator;

/**
 * Invokes a method on an object with an argument list.
 * <p/>
 * The object is specified by the container, e.g. for a HTTP request, it's the HTTP URL.<br>
 * The arguments are specified by Hessian serialization.
 * <p/>
 * Grammar call ::= c x02 x00 m b1 b0 <method-string> value* z
 */
public class HessianCall extends HessianSimple
{
    private final String method;

    private List<HessianValue> arguments;
    private boolean mangled = true;

    public HessianCall(final String aMethod)
    {
        this(aMethod, new LinkedList<HessianValue>());
    }

    public HessianCall(final String aMethod, final List<HessianValue> someArguments)
    {
        method = aMethod;
        arguments = someArguments;
    }

    public void setMangled(boolean aVal)
    {
        mangled = aVal;
    }

    public void addArgument(HessianValue anArgument)
    {
        arguments.add(anArgument);
    }

    public int size()
    {
        return arguments.size();
    }

    public Iterator<HessianValue> iterator()
    {
        return arguments.iterator();
    }

    public HessianValue getArgument(int aIndex)
    {
        return arguments.get(aIndex);
    }

    public String getMethod()
    {
        return method;
    }

    public List<HessianValue> getArguments()
    {
        return arguments;
    }

    /**
     * Renders a Hessian call.
     *
     * @param aStream
     * @param types
     * @param classDefs
     * @param objects
     * @throws HessianRenderException
     * @see HessianCall
     */
    public void render(OutputStream aStream, List<HessianString> types, List<HessianClassdef> classDefs, List<HessianComplex> objects) throws HessianRenderException
    {
        //'c' x02 x00 method value* 'z'
        try
        {
            aStream.write('c');
            aStream.write(0x02);
            aStream.write(0x00);

            //Render the method name (with optional mangling)
            aStream.write('m');
            byte[] lUtf8 = (mangled) ? getMangledMethod().getBytes("UTF8") : method.getBytes("UTF8");
            aStream.write((byte) (lUtf8.length >> 8));
            aStream.write((byte) lUtf8.length);
            aStream.write(lUtf8);

            for (final HessianValue lArg : arguments)
            {
                lArg.render(aStream, types, classDefs, objects);
            }

            aStream.write('z');
        }
        catch (IOException e)
        {
            throw new HessianRenderException(e);
        }
    }

    /**
     * Mangles the method name with its argument types to support overloaded methods.
     *
     * @return
     */
    private String getMangledMethod()
    {
        if (arguments.isEmpty())
            return method;
        else
        {
            StringBuilder lResult = new StringBuilder(method);
            for (HessianValue lArg : arguments)
                lResult.append("_").append(lArg.getTypeString());
            return lResult.toString();
        }
    }

    protected String prettyPrint(String aIndent, List<HessianClassdef> classdefs, List<HessianComplex> objects)
    {
        final StringBuilder lBuilder = new StringBuilder();
        lBuilder.append(aIndent);
        lBuilder.append("<call").append(":'").append(method).append("'>");
        for (int i = 0; i < arguments.size(); i++)
        {
            String lSubIndent = aIndent + "    ";
            lBuilder.append("\n");
            String lSub = arguments.get(i).prettyPrint(lSubIndent, classdefs, objects);
            lBuilder.append(aIndent);
            lBuilder.append("[").append(i).append("] ").append(lSub.substring(lSubIndent.length()));
        }
        return lBuilder.toString();
    }
}
