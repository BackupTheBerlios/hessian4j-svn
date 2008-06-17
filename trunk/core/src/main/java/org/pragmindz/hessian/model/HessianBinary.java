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
import java.util.Iterator;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Models binary data as variable length byte-chunks.
 * <p>
 * Grammar :
 * <ul>
 * <li> b b1 b0 &lt;binary-data&gt;
 * <li> B b1 b0 &lt;binary-data&gt;
 * <li> [x20-x2f] &lt;binary-data&gt; : binary data of length 0-15
 * </ul>
 * Binary data is encoded in (variable length) chunks.<br>
 * The octet x42 ('B') encodes the final chunk and x62 ('b') represents any non-final chunk.<br>
 * Each chunk has a 16-bit length value (len = 256 * b1 + b0)
 */
public class HessianBinary
extends HessianSimple
{
    private List<byte[]> data = new LinkedList<byte[]>();

    public void add(byte[] aChunk)
    {
        data.add(aChunk);
    }

    public Iterator<byte[]> iterator()
    {
        return data.iterator();
    }

    public byte[] get(int aIndex)
    {
        return data.get(aIndex);
    }

    public int size()
    {
        return data.size();
    }

    public void render(OutputStream aStream, List<HessianString> types, List<HessianClassdef> classDefs, List<HessianComplex> objects) throws HessianRenderException
    {
        //spec: 8-bit binary data split into 64k chunks
        //We don't force the chunks to be 64k in this implementation
        try
        {
            //[x20-x2f] <binary-data> : binary data of length 0-15
            int lLength = get(0).length;
            if (size() == 1 && lLength <= 0x0f)
            {
                aStream.write(0x20 + (byte) lLength);
                aStream.write(get(0));
            }
            else
            {
                if (size() > 1)
                {
                    //'b' b1 b0 <binary-data> binary # non-final chunk
                    for (int i = 0; i < size() - 1; i++)
                    {
                        aStream.write('b');
                        aStream.write((byte) (get(i).length >> 8));
                        aStream.write((byte) get(i).length);
                        aStream.write(get(i));
                    }
                }
                //'B' b1 b0 <binary-data> # final chunk
                aStream.write('B');
                aStream.write((byte) (get(size()-1).length >> 8));
                aStream.write((byte) get(size()-1).length);
                aStream.write(get(size()-1));
            }
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
        lBuilder.append("<binary>");
        return lBuilder.toString();
    }
}