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
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;

public abstract class HessianComplex
extends HessianValue
{
    void renderReference(OutputStream aStream, int aValueRef) throws IOException
    {
        if (aValueRef <= 0xff)
        {
            aStream.write(0x4a);
            aStream.write((byte) aValueRef);
        }
        else if (aValueRef <= 0xffff)
        {
            aStream.write(0x4b);
            aStream.write((byte) (aValueRef >> 8));
            aStream.write((byte) aValueRef);
        }
        else
        {
            aStream.write('R');
            aStream.write((byte) (aValueRef >> 24));
            aStream.write((byte) (aValueRef >> 16));
            aStream.write((byte) (aValueRef >> 8));
            aStream.write((byte) aValueRef);
        }
    }

    void renderTypeString(OutputStream aStream, List<HessianString> types, HessianString aType)
    throws IOException
    {
        // Type string is optional.
        final String lValue = aType.getValue();
        if(lValue != null)
        {
            // Check if we have this type already in our types-list;
            int lTypeRef = types.indexOf(aType);
            // Only short type refs (1 octet are allowed).
            // So if the ref is larger we have to repeat the whole type name.
            if (lTypeRef != -1 && lTypeRef <= 255)
            {
                aStream.write(0x75);
                aStream.write((byte) lTypeRef);
            }
            else
            {
                aStream.write('t');
                final byte[] lUtf8 = lValue.getBytes("UTF8");
                final int lLength = lUtf8.length;
                aStream.write((byte) (lLength >> 8));
                aStream.write((byte) lLength);
                aStream.write(lUtf8);
                types.add(aType);
            }
        }
    }
}
