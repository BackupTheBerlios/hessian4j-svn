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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Models a Hessian Object Definition.<br>
 * An object definition includes a mandatory type string, the number of fields, and the field names.<br>
 * The object definition is stored in the object definition map and will be referenced by object instances with an integer reference.
 * <p>
 * Grammar : class-def  ::= 'O' string int string*
 */
public class HessianClassdef
extends HessianSimple
{
    private HessianString type;
    private List<HessianString> fieldNames = new ArrayList<HessianString>();

    public HessianClassdef(HessianString type)
    {
        this.type = type;
    }    

    public HessianClassdef(String aType)
    {
        this.type = new HessianString(aType);
    }

    public HessianClassdef(String aType, String ... aFieldNames)
    {
        this(aType);
        for(String lFieldName: aFieldNames) this.add(new HessianString(lFieldName));
    }

    public void add(HessianString aFieldName)
    {
        fieldNames.add(aFieldName);
    }

    public int size()
    {
        return fieldNames.size();
    }

    public Iterator<HessianString> iterator()
    {
        return fieldNames.iterator();
    }

    public HessianString getFieldName(int aIndex)
    {
        return fieldNames.get(aIndex);
    }

    public int getFieldIndex(String aFieldName)
    {
        int i = 0;
        for(HessianString lCandidate:fieldNames)
        {
            if(aFieldName.equals(lCandidate.getValue())) return i;
            else i++;
        }
        return -1;
    }

    public HessianString getType()
    {
        return type;
    }

    /**
     * Renders a Hessian Object Definition
     *
     * @see HessianClassdef
     * @param aStream
     * @param types
     * @param classDefs
     * @param objects
     * @throws HessianRenderException
     */
    public void render(OutputStream aStream, List<HessianString> types, List<HessianClassdef> classDefs, List<HessianComplex> objects) throws HessianRenderException
    {
        try
        {
            aStream.write('O');

            //Render the type string
            //Note that we don't render it as a normal type string (we omit the 't' and render the length as an 'int')
            final byte[] lUtf8 = type.getValue().getBytes("UTF8");
            final int lLength = lUtf8.length;
            HessianInteger.valueOf(lLength).render(aStream, types, classDefs, objects);
            aStream.write(lUtf8);

            //Render the field count
            HessianInteger.valueOf(size()).render(aStream, types, classDefs, objects);

            //Render the field names
            for (HessianString lField : fieldNames)
            {
                lField.render(aStream, types, classDefs, objects);
            }

            //Add it to the classDefs
            classDefs.add(this);
        }
        catch(IOException e)
        {
            throw new HessianRenderException(e);
        }        
    }

    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        final HessianClassdef that = (HessianClassdef) o;

        return !(type != null ? !type.equals(that.type) : that.type != null);

    }

    public int hashCode()
    {
        return (type != null ? type.hashCode() : 0);
    }

    protected String prettyPrint(String aIndent, List<HessianClassdef> aClassdefs, List<HessianComplex> aObjects)
    {
        int lDefRef = aClassdefs.indexOf(this);
        String lDefRefRepr = "#???";
        if(lDefRef >= 0) lDefRefRepr = "#" + lDefRef;

        final StringBuilder lBuilder = new StringBuilder();
        lBuilder.append(aIndent);
        lBuilder.append("<classdef").append(lDefRefRepr);
        if(type != null) lBuilder.append(":'").append(type.getValue()).append("'");
        lBuilder.append(">");
        for(int i = 0; i < fieldNames.size(); i++)
        {
            lBuilder.append("\n");
            lBuilder.append(aIndent);
            lBuilder.append("[").append(i).append("] ").append(fieldNames.get(i).getValue());
        }
        return lBuilder.toString();
    }
}