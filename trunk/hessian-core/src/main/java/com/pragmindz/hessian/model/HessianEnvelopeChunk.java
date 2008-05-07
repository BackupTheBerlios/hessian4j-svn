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

import java.util.List;
import java.util.Map;
import java.io.OutputStream;

public class HessianEnvelopeChunk extends HessianSimple
{
    private final Map<HessianString, HessianValue> header;
    private final Map<HessianString, HessianValue> footer;

    private final HessianBinary body;

    public HessianEnvelopeChunk(final HessianBinary aBody)
    {
        this(aBody, null, null);
    }

    public HessianEnvelopeChunk(final HessianBinary aBody, final Map<HessianString, HessianValue> aHeader, final Map<HessianString, HessianValue> aFooter)
    {
        header = aHeader;
        footer = aFooter;
        body = aBody;
    }

    public Map<HessianString, HessianValue> getHeader()
    {
        return header;
    }

    public Map<HessianString, HessianValue> getFooter()
    {
        return footer;
    }

    public HessianBinary getBody()
    {
        return body;
    }

    public void render(OutputStream aStream, List<HessianString> types, List<HessianClassdef> classDefs, List<HessianComplex> objects)
    throws HessianRenderException
    {
        //Render the header
       HessianInteger.valueOf(header.size()).render(aStream, types, classDefs, objects);
        for (Map.Entry<HessianString, HessianValue> lHeader : header.entrySet())
        {
            lHeader.getKey().render(aStream, types, classDefs, objects);
            lHeader.getValue().render(aStream, types, classDefs, objects);
        }

        //Render the body
        body.render(aStream, types, classDefs, objects);

        //Render the footer
        HessianInteger.valueOf(footer.size()).render(aStream, types, classDefs, objects);
        for (Map.Entry<HessianString, HessianValue> lFooter : footer.entrySet())
        {
            lFooter.getKey().render(aStream, types, classDefs, objects);
            lFooter.getValue().render(aStream, types, classDefs, objects);
        }
    }

    protected String prettyPrint(String aIndent, List<HessianClassdef> aClassdefs, List<HessianComplex> aObjects)
    {
        final StringBuilder lBuilder = new StringBuilder();
        lBuilder.append(aIndent);
        lBuilder.append("<envelope-chunk>");
        return lBuilder.toString();
    }
}