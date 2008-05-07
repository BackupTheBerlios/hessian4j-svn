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
import com.sun.org.apache.xpath.internal.operations.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.lang.String;

/**
 * Models boolean data.
 * <p>
 * The octet 'F' represents false and the octet T represents true.
 */
public class HessianBoolean
extends HessianSimple
{
    public static final HessianBoolean TRUE = new HessianBoolean(true);
    public static final HessianBoolean FALSE = new HessianBoolean(false);
    public static HessianBoolean valueOf(boolean aValue)
    {
        return aValue?TRUE:FALSE;
    }

    private boolean value;

    public HessianBoolean(boolean value)
    {
        this.value = value;
    }

    public boolean isValue()
    {
        return value;
    }

    @Override
    public String getTypeString()
    {
        return "boolean";
    }

    /**
     * Renders the boolean as 'T' for true or 'F' for false.
     *
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
            aStream.write(value?'T':'F');
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
        lBuilder.append("<bool:").append(value).append(">"); 
        return lBuilder.toString();
    }
}