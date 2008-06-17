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

import java.io.OutputStream;
import java.io.IOException;
import java.util.List;

/**
 Models a 64-bit signed integer.<br>
 A long is represented by the octet x4c ('L' ) followed by the 8-bytes of the integer in big-endian order.
 <code><pre>
 Grammar
 long ::= L b7 b6 b5 b4 b3 b2 b1 b0 64-bit signed long integer
      ::= [xd8-xef] one-octet compact long (-x8 to x10, xe0 is 0)
      ::= [xf0-xff] b0 two-octet compact long (-x800 to x7ff, xf8 is 0)
      ::= [x38-x3f] b1 b0 3-octet long (-x40000 to x3ffff, x3c is 0)
      ::= x77 b3 b2 b1 b0 long encoded as 32-bit int
</code></pre>
 */
public class HessianLong
extends HessianSimple    
{
    private static final int LONGCACHE = 50;
    private static final HessianLong[] positives = new HessianLong[LONGCACHE];
    private static final HessianLong[] negatives = new HessianLong[LONGCACHE];
    static
    {
        for(int i = 0; i < LONGCACHE; i++)
        {
            positives[i] = new HessianLong(i);
            negatives[LONGCACHE - 1  - i] = new HessianLong(-i);
        }
    }

    public static HessianLong valueOf(long aValue)
    {
        if(aValue >= 0 && aValue < LONGCACHE) return positives[(int) aValue];
        else if(aValue < 0 && aValue > -LONGCACHE) return negatives[LONGCACHE - 1 + (int) aValue];
        else return new HessianLong(aValue);
    }

    private final long value;

    public HessianLong(final long value)
    {
        this.value = value;
    }

    public long getValue()
    {
        return value;
    }

    @Override
    public String getTypeString()
    {
        return "long";
    }

    /**
     * Renders a HessianLong.
     * 
     * @see org.pragmindz.hessian.model.HessianLong
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
            if (value >= -0x08 && value <= 0x0f)
            {
                //one-octet compact long (-x8 to x10, xe0 is 0)
                aStream.write(0xe0 + (byte)value);
            }
            else if (value >= -0x800 && value <= 0x7ff)
            {
                //two-octet compact long (-x800 to x7ff, xf8 is 0)
                aStream.write((0xf8 + (byte)(value >> 8)));
                aStream.write((byte)value);
            }
            else if (value >= -0x40000 && value <= 0x3ffff)
            {
                //3-octet long (-x40000 to x3ffff, x3c is 0)
                 aStream.write((0x3c + (byte)(value >> 16)));
                 aStream.write((byte)(value >> 8));
                 aStream.write((byte)value);
            }
            else if ((int)value == value)
            {
                //long encoded as 32-bit int
                aStream.write(0x77);
                aStream.write((byte)(value >> 24));
                aStream.write((byte)(value >> 16));
                aStream.write((byte)(value >> 8));
                aStream.write((byte)value);
            }
            else
            {
                //64-bit signed long integer
                aStream.write('L');
                aStream.write((byte)(value >> 56));
                aStream.write((byte)(value >> 48));
                aStream.write((byte)(value >> 40));
                aStream.write((byte)(value >> 32));
                aStream.write((byte)(value >> 24));
                aStream.write((byte)(value >> 16));
                aStream.write((byte)(value >> 8));
                aStream.write((byte) value);
            }
        }
        catch(IOException e)
        {
            throw new HessianRenderException(e);
        }
    }

    protected String prettyPrint(String aIndent, List<HessianClassdef> aClassdefs, List<HessianComplex> aObjects)
    {
        final StringBuilder lBuilder = new StringBuilder();
        lBuilder.append(aIndent);
        lBuilder.append("<long:").append(value).append(">");
        return lBuilder.toString();
    }
}