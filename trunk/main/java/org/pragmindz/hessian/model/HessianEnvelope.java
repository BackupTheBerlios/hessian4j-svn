package org.pragmindz.hessian.model;
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
import org.pragmindz.hessian.parser.HessianRenderException;

import java.util.List;
import java.util.LinkedList;
import java.io.OutputStream;
import java.io.IOException;

/**
A Hessian envelope wraps a Hessian message, adding headers and footers and possibly compressing or encrypting the wrapped message.<br>
The envelope type is identified by a method string.
<p>
Envelopes can contain multiple chunks of data.<br>
Each chunk can contain multiple headers and/or footers.
<p>
Grammar envelope ::= E x02 x00 m b1 b0 <method-string> env-chunk* z
<p>
Grammar env-chunk ::= int (string value)* binary int (string value)*
 */
public class HessianEnvelope
extends HessianSimple
{
    private final String method;
    private List<HessianEnvelopeChunk> chunks = new LinkedList<HessianEnvelopeChunk>();

    public HessianEnvelope(String aMethod)
    {
        method = aMethod;
    }

    public void add(HessianEnvelopeChunk aChunk)
    {
        chunks.add(aChunk);
    }

    public int size()
    {
        return chunks.size();
    }

    public String getMethod()
    {
        return method;
    }

    public List<HessianEnvelopeChunk> getChunks()
    {
        return chunks;
    }


    /**
     * Renders a Hessian envelope to the outputstream.
     *
     * @see HessianEnvelope
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
            if (chunks.isEmpty())
                throw new HessianRenderException("Envelope should contain at least one chunk");

            aStream.write('E');
            aStream.write(0x02);
            aStream.write(0x00);

            //Render the method name
            aStream.write('m');
            byte[] lUtf8 = method.getBytes("UTF8");
            aStream.write((byte) (lUtf8.length >> 8));
            aStream.write((byte) lUtf8.length);
            aStream.write(lUtf8);

            for (HessianEnvelopeChunk lChunk : chunks)
            {
                lChunk.render(aStream, types, classDefs, objects);
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
        lBuilder.append("<envelope>");
        return lBuilder.toString();
    }

    public HessianEnvelopeChunk get(final int i)
    {
        return chunks.get(i);
    }
}
