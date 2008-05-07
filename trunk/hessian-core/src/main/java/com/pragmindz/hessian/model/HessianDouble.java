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
import java.util.List;

/**
Models a 64-bit IEEE floating pointer number.
 <p>
The double 0.0 can be represented by the octet x67<br>
The double 1.0 can be represented by the octet x68<br>
Doubles between -128.0 and 127.0 with no fractional component can be represented in two octets by casting the byte value to a double.<br>
Doubles between -32768.0 and 32767.0 with no fractional component can be represented in three octets by casting the short value to a double.<br>
Doubles which are equivalent to their 32-bit float representation can be represented as the 4-octet float and then cast to double.<br>
 <p>
Grammar : double
 <ul>
 <li>      ::= D b7 b6 b5 b4 b3 b2 b1 b0
<li>       ::= x67
<li>       ::= x68
<li>       ::= x69 b0
<li>       ::= x6a b1 b0
<li>       ::= x6b b3 b2 b1 b0
 </ul>
 */
public class HessianDouble
extends HessianSimple
{
    public static final HessianDouble ZERO = new HessianDouble(0.0);
    public static final HessianDouble ONE = new HessianDouble(1.0);

    private final double value;

    public HessianDouble(final double value)
    {
        this.value = value;
    }

    public double getValue()
    {
        return value;
    }

    @Override
    public String getTypeString()
    {
        return "double";
    }

    /**
     * Renders a double.
     *
     * @see HessianDouble
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
            if (((short) value) == value)
            {
                if (value == 0.0D) aStream.write(0x67);
                else if (value == 1.0D) aStream.write(0x68);
                else if (value >= -128 && value < 127)
                {
                    //double represented as byte (-128.0 to 127.0)
                    aStream.write(0x69);
                    aStream.write((byte) value);
                }
                else if (value >= -32768 && value <= 32767)
                {
                    //double represented as short (-32768 to 32767)
                    aStream.write(0x6a);
                    aStream.write((byte) (((short) value) >> 8));
                    aStream.write((byte) value);
                }
                return;
            }
            if (((float) value) == value)
            {
                //double represented as float
                int lIntBits = Float.floatToIntBits((float) value);
                aStream.write(0x6b);
                aStream.write((byte) (lIntBits >> 24));
                aStream.write((byte) (lIntBits >> 16));
                aStream.write((byte) (lIntBits >> 8));
                aStream.write((byte) lIntBits);
            }
            else
            {
                //64-bit IEEE encoded double ('D')
                long lLongBits = Double.doubleToLongBits(value);
                aStream.write('D');
                aStream.write((byte) (lLongBits >> 56));
                aStream.write((byte) (lLongBits >> 48));
                aStream.write((byte) (lLongBits >> 40));
                aStream.write((byte) (lLongBits >> 32));
                aStream.write((byte) (lLongBits >> 24));
                aStream.write((byte) (lLongBits >> 16));
                aStream.write((byte) (lLongBits >> 8));
                aStream.write((byte) lLongBits);
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
        lBuilder.append("<double:").append(value).append(">");
        return lBuilder.toString();
    }
}