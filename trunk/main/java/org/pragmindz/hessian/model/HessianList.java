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
 Models an ordered list of {@link HessianValue}.
 <p>
 All lists have a type string, a length, a list of values, and a trailing octet x7a ('z').<br>
 The type string may be an arbitrary UTF-8 string understood by the service.<br>
 The length may be omitted to indicate that the list is variable length.
 <p>
 Each list item is added to the reference list to handle shared and circular elements. See the ref element.
 <code><pre>
 Grammar
 list ::= V type? length? value* z
      ::= v int int value*
</pre></code>

 */
public class HessianList
extends HessianComplex
{
    private HessianString type;
    private List<HessianValue> data;
    private boolean variable;

    public HessianList()
    {
        type = null;
        variable = true;
        data = new LinkedList<HessianValue>();
    }

    public HessianList(HessianString type)
    {
        this();
        this.type = type;
    }

    public void add(HessianValue aVal)
    {
        data.add(aVal);
    }

    public Iterator<HessianValue> iterator()
    {
        return data.iterator();
    }

    public void setType(HessianString type)
    {
        this.type = type;
    }

    public boolean hasTypeString()
    {
        return type != null;
    }

    public HessianString getType()
    {
        return type;
    }

    public boolean isVariable()
    {
        return variable;
    }

    public void setVariable(boolean variable)
    {
        this.variable = variable;
    }

    public int size()
    {
        return data.size();
    }

    @Override
    public String getTypeString()
    {
        return "list";
    }

    public void render(OutputStream aStream, List<HessianString> types, List<HessianClassdef> classDefs, List<HessianComplex> objects)
    throws HessianRenderException
    {
        try
        {
            final int lValueRef = objects.indexOf(this);
            if (lValueRef != -1)
            {
                renderReference(aStream, lValueRef);
            }
            else
            {
                // Add it to the object refs.
                objects.add(this);

                if(hasTypeString() && types.contains(getType()) && !isVariable())
                {
                    // Short form possible.
                    // Both a known type (can be a long ref > 255) and a known length are needed.
                    aStream.write('v');
                    HessianInteger.valueOf(types.indexOf(getType())).render(aStream, types, classDefs, objects);
                    HessianInteger.valueOf(size()).render(aStream, types, classDefs, objects);
                    // Write the values.
                    for (HessianValue lVal : data) lVal.render(aStream, types, classDefs, objects);
                }
                else
                {
                    // Verbose form needed.
                    aStream.write('V');
                    if (hasTypeString()) renderTypeString(aStream, types, type);
                    // Write the length, can be omitted for variable length lists.
                    if (!isVariable())
                    {
                        if (size() < 256)
                        {
                            aStream.write(0x6e);
                            aStream.write((byte) size());
                        }
                        else
                        {
                            aStream.write('l');
                            aStream.write((byte) (size() >> 24));
                            aStream.write((byte) (size() >> 16));
                            aStream.write((byte) (size() >> 8));
                            aStream.write((byte) size());
                        }
                    }
                    //Write the values
                    for (HessianValue lVal : data) lVal.render(aStream, types, classDefs, objects);
                    //Write end-of-list marker
                    aStream.write('z');
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
        if(aObjects.contains(this))
        {
            int lRef = aObjects.indexOf(this);
            return aIndent + "<ref:" + lRef + ">";
        }
        else
        {
            aObjects.add(this);
            final StringBuilder lBuilder = new StringBuilder();
            lBuilder.append(aIndent);
            lBuilder.append("<list").append("@").append(aObjects.size() -1);
            if(type != null) lBuilder.append(":'").append(type.getValue()).append("'");
            if(!variable) lBuilder.append(":").append(data.size());
            lBuilder.append(">");
            for(int i = 0; i < data.size(); i++)
            {
                String lSubIndent = aIndent + "    ";
                lBuilder.append("\n");
                String lSub = data.get(i).prettyPrint(lSubIndent, aClassdefs, aObjects);
                lBuilder.append(aIndent);
                lBuilder.append("[").append(i).append("] ").append(lSub.substring(lSubIndent.length()));
            }
            return lBuilder.toString();
        }
    }
}