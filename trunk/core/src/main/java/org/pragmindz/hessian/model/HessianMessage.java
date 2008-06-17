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
import java.util.ArrayList;

/**
 * Models a Hessian message as a list of {@link org.pragmindz.hessian.model.HessianValue} objects.
 * <code><pre>
 * Grammar
 * message   ::= ('p' b1 b0 data)* 'P' b1 b0 data
 * </code></pre>
 *
 */
public class HessianMessage extends HessianSimple
{
    private List<HessianValue> values = new ArrayList<HessianValue>();

    public void addValue(HessianValue aValue)
    {
        values.add(aValue);
    }

    public List<HessianValue> getValues()
    {
        return values;
    }

    public int size()
    {
        return values.size();
    }

    public HessianValue get(int index)
    {
        return values.get(index);
    }

    /**
     * Renders a HessianMessage.
     *
     * @see org.pragmindz.hessian.model.HessianMessage
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
            aStream.write('p');
            aStream.write(0x02);
            aStream.write(0x00);
            for (HessianValue lValue : values)
            {
                lValue.render(aStream, types, classDefs, objects);
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
        lBuilder.append("<message:").append(size()).append(">");
        for(int i = 0; i < size(); i++)
        {
            String lSubIndent = aIndent + "    ";
            lBuilder.append("\n");
            String lSub = values.get(i).prettyPrint(lSubIndent, aClassdefs, aObjects);
            lBuilder.append(aIndent);
            lBuilder.append("[").append(i).append("] ").append(lSub.substring(lSubIndent.length()));
        }
        return lBuilder.toString();
    }
}
