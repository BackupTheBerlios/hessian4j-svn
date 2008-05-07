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
 * Models a fault-reply as a result of a call.
 * <br>
 * A fault reply contains a fault modelled as a {@link com.pragmindz.hessian.model.HessianFault} and possibly some header
 * information.
 * <br>
 * Headers are not yet supported in this implementation.
 * <br>
 * <code><pre>
 *  Grammar
 *   fault-reply ::= r x02 x00 header* fault z
 * </pre></code>
 * 
 */
//TODO Implement headers
public class HessianFaultReply extends HessianReply
{
    private final HessianFault fault;

    public HessianFaultReply(HessianFault aFault)
    {
        fault = aFault;
    }

    public HessianFault getFault()
    {
        return fault;
    }

    /**
     * Renders a Hessian fault reply.
     *
     * @see com.pragmindz.hessian.model.HessianFaultReply
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
            aStream.write('r');
            aStream.write(0x02);
            aStream.write(0x00);
            fault.render(aStream, types, classDefs, objects);
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
        lBuilder.append("<fault>"); 
        return lBuilder.toString();
    }
}
