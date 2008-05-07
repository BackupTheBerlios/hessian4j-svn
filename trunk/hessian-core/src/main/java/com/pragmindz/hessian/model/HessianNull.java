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
 * Models a Hessian null value.
 * <p>
 * The octet 'N' represents the null value.
 * <br>
 * <code><pre>
 * Grammar
 *   null ::= N
 * </pre></code>
 */
public final class HessianNull
extends HessianSimple
{
    public static final HessianNull NULL = new HessianNull();

    private HessianNull()
    {
    }

    /**
     * Renders a Hessian null value.
     *
     * @see com.pragmindz.hessian.model.HessianNull
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
            aStream.write('N');
        }
        catch (IOException e)
        {
            throw new HessianRenderException(e);
        }
    }

    public boolean equals(Object obj)
    {
        return obj instanceof HessianNull;
    }

    public int hashCode()
    {
        return 113;
    }

    protected String prettyPrint(String aIndent, List<HessianClassdef> aClassdefs, List<HessianComplex> aObjects)
    {
        final StringBuilder lBuilder = new StringBuilder();
        lBuilder.append(aIndent);
        lBuilder.append("<null>");
        return lBuilder.toString();
    }
}
