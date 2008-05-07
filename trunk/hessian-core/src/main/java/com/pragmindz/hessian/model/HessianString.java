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
 * Models a 16-bit unicode character string encoded in UTF-8.
 * <p>
 * Strings are split in variable length chunks. The default chunk size is 64k.
 * <br>
 * 'S' represents the final chunk and 's' represents any non-final chunk.
 * <br>Each chunk has a 16-bit length value.
 * <br>Please note that the length can be different from the actual string length if we are dealing with
 * non-ascii chars.
 * <br>Strings with length less than 32 may be encoded with a single octet length [x00-x1f].
 * <code><pre>
 * Grammar :
 * string ::= s b1 b0 <utf8-data> string
 *        ::= S b1 b0 <utf8-data>
 *        ::= [x00-x1f] <utf8-data>
 * </pre></code>
 */
public class HessianString
extends HessianSimple
{
    private final StringBuilder value;
    private int chunkSize = 0xffff; //default = 64kbyte chunks

    public HessianString(String aVal)
    {
        this.value = new StringBuilder(aVal);
    }

    public int getChunkSize()
    {
        return chunkSize;
    }

    public void setChunkSize(final int aChunkSize)
    {
        chunkSize = aChunkSize;
    }

    public String getValue()
    {
        return value.toString();
    }

    public void add(String aVal)
    {
        value.append(aVal);
    }

    public int getLength()
    {
        return value.length();
    }

    public boolean equals(Object o)
    {
        return o == this || o instanceof HessianString && getValue().equals(((HessianString) o).getValue());
    }

    public int hashCode()
    {
        return getValue().hashCode();
    }

    @Override
    public String getTypeString()
    {
        return "string";
    }

    /**
     * Renders a Hessian string value.
     *
     * @see com.pragmindz.hessian.model.HessianString
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
            if (value == null) aStream.write('N');
            else
            {
                // Byte buffer and byte buffer length should be used to
                // calculate the lengths, not the string length because a non ascii character
                // might be represented by multiple bytes.
                byte[] lUtf8 = value.toString().getBytes("UTF8");
                int lLength = lUtf8.length;

                if (lLength < 32)
                {
                    //x00 - x1f : utf-8 string length 0-31
                    aStream.write((byte) lLength);
                    aStream.write(lUtf8);
                }
                else
                {
                    //Split the string into 64kbyte chunks
                    int lOffset = 0;
                    while (lLength > chunkSize)
                    {
                        //utf-8 string non-final chunk ('s')
                        aStream.write('s');
                        aStream.write((byte)(chunkSize >> 8));
                        aStream.write((byte) chunkSize);
                        aStream.write(lUtf8,lOffset, chunkSize);
                        lLength -= chunkSize;
                        lOffset += chunkSize;
                    }

                    if (lLength > 0)
                    {
                        //utf-8 string final chunk ('S')
                        aStream.write('S');
                        aStream.write((byte) (lLength >> 8));
                        aStream.write((byte) (lLength));
                        aStream.write(lUtf8,lOffset,lLength);
                    }
                }
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
        lBuilder.append("<string:\"").append(value).append("\">");
        return lBuilder.toString();
    }
}
