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
import java.util.List;
import java.io.IOException;

/**
Models a 32-bit signed integer.
<p>
An integer is represented by the octet x49 ('I') followed by the 4 octets of the integer in big-endian order.<br>
value = (b3 &lt;&lt; 24) + (b2 &lt;&lt; 16) + (b1 &lt;&lt; 8) + b0;
 <code><pre>
 Grammar
    int ::= 'I' b3 b2 b1 b0
        ::= [x80-xbf]             # -x10 to x3f
        ::= [xc0-xcf] b0          # -x800 to x7ff
        ::= [xd0-xd7] b1 b0       # -x40000 to x3ffff
 </pre></code>
 */
public class HessianInteger
extends HessianSimple
{
    private static final int INTCACHE = 50;
    private static final HessianInteger[] positives = new HessianInteger[INTCACHE];
    private static final HessianInteger[] negatives = new HessianInteger[INTCACHE];
    static
    {
        for(int i = 0; i < INTCACHE; i++)
        {
            positives[i] = new HessianInteger(i);
            negatives[INTCACHE - 1  - i] = new HessianInteger(-i);
        }
    }

    public static HessianInteger valueOf(int aValue)
    {
        if(aValue >= 0 && aValue < INTCACHE) return positives[aValue];
        else if(aValue < 0 && aValue > -INTCACHE) return negatives[INTCACHE - 1 + aValue];
        else return new HessianInteger(aValue);
    }

    private final int value;

    public HessianInteger(final int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    @Override
    public String getTypeString()
    {
        return "int";
    }

    public void render(OutputStream aStream, List<HessianString> types, List<HessianClassdef> classDefs, List<HessianComplex> objects)
    throws HessianRenderException
    {
        try
        {
            // one-octet compact int (-x10 to x2f, x90 is 0)
            if (value >= -16 && value <= 47)
            {
                aStream.write(value + 0x90);
            }
            //two-octet compact int (-x800 to x3ff)
            //Integers between -2048 and 2047
            else if (value >= -2048 && value <= 2047)
            {
                aStream.write((byte) (0xc8 + (value >> 8)));
                aStream.write((byte) value);
            }
            //Integers between -262144 and 262143
            // three-octet compact int (-x40000 to x3ffff).
            else if (value >= -262144 && value <= 262143)
            {
                aStream.write((byte) (0xd4 + (value >> 16)));
                aStream.write((byte) (value >> 8));
                aStream.write((byte) value);
            }
            // 32-bit signed integer ('I').
            else
            {
                aStream.write('I');
                aStream.write((byte) (value >> 24));
                aStream.write((byte) (value >> 16));
                aStream.write((byte) (value >> 8));
                aStream.write((byte) value);
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
        lBuilder.append("<int:").append(value).append(">");
        return lBuilder.toString();
    }
}
