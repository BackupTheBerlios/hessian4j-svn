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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

/**
Date represented by a 64-bit long of milliseconds since the Jan 1 1970 00:00H, UTC.
<p>
Grammar date ::= d b7 b6 b5 b4 b3 b2 b1 b0
 */
public class HessianDate
extends HessianSimple
{
    private final Date value;

    public HessianDate(final Date value)
    {
        this.value = value;
    }

    public Date getValue()
    {
        return value;
    }

    @Override
    public String getTypeString()
    {
        return "date";
    }

    /**
     * Renders a Hessian date.
     *
     * @see HessianDate
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
            //UTC time encoded as 64-bit long milliseconds since epoch ('d')
            long lTime = value.getTime();
            aStream.write('d');
            aStream.write((byte)(lTime >> 56));
            aStream.write((byte)(lTime >> 48));
            aStream.write((byte)(lTime >> 40));
            aStream.write((byte)(lTime >> 32));
            aStream.write((byte)(lTime >> 24));
            aStream.write((byte)(lTime >> 16));
            aStream.write((byte)(lTime >> 8));
            aStream.write((byte)lTime);
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
        lBuilder.append("<date:").append(value.toString()).append(">");
        return lBuilder.toString();
    }
}