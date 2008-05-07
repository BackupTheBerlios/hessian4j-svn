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

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Models a Hessian object.
 * <br>
 * A hessian object consists out of an object definition (which is modelled as a {@link com.pragmindz.hessian.model.HessianClassdef})
 * and a list of field values that are modelled as {@link com.pragmindz.hessian.model.HessianValue} objects.
 * <br>
 * Hessian 2.0 has a compact object form where the field names are only serialized once.<br>
 * Following objects only need to serialize their values.
 * <p>
 * Every rendered object will be kept in a reference-list. If the same object is rendered more then once, a reference will be rendered
 * that points to the entry in the reference-list.
 * <br>
 * <code><pre>
 * Grammar
 *   object     ::= 'o' int value*
 * </pre></code>
 *
 */
public class HessianObject
extends HessianComplex
{
    private HessianClassdef hessianClassdef;
    private List<HessianValue> fields = new LinkedList<HessianValue>();

    public HessianObject(HessianClassdef hessianClassdef)
    {
        this.hessianClassdef = hessianClassdef;
    }

    public HessianClassdef getHessianClassdef()
    {
        return hessianClassdef;
    }

    public void add(HessianValue aField)
    {
        fields.add(aField);
    }

    public int size()
    {
        return fields.size();
    }

    public Iterator<HessianValue> iterator()
    {
        return fields.iterator();
    }

    public HessianValue getField(int aIndex)
    {
        return fields.get(aIndex);
    }

    @Override
    public String getTypeString()
    {
        //return the object definition name
        return hessianClassdef.getType().getValue();
    }

    /**
     * Find the field value of an object using the field name.
     * @param aFieldName The name of the field as a plain string.
     * @return The value.
     */
    public HessianValue getField(String aFieldName)
    {
        int lIndex = hessianClassdef.getFieldIndex(aFieldName);
        if(lIndex >= 0) return getField(lIndex);
        else return null;
    }

    /**
     * Renders a Hessian object.
     *
     * @see com.pragmindz.hessian.model.HessianObject
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
            //Check if this is the first time that we render this object
            int lObjPos = objects.indexOf(this);
            if (lObjPos == -1)
            {
                //Check if this is the first time we render an object of this class
                int lPos = classDefs.indexOf(hessianClassdef);
                if (lPos == -1)
                {
                    //Render the class definition
                    hessianClassdef.render(aStream, types, classDefs, objects);
                    lPos = classDefs.indexOf(hessianClassdef);
                }

                //Add it to the object references
                objects.add(this);

                //Render the object instance
                aStream.write('o');
                HessianInteger.valueOf(lPos).render(aStream, types, classDefs, objects);
                for (HessianValue lVal : fields)
                {
                    lVal.render(aStream, types, classDefs, objects);
                }         
            }
            else
            {
                //Render the object reference
                renderReference(aStream, lObjPos);
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
            lBuilder.append("<object").append("@").append(aObjects.size() -1);
            if(hessianClassdef != null)
            {
                int lDefRef = aClassdefs.indexOf(hessianClassdef);
                if(lDefRef < 0)
                {
                    aClassdefs.add(hessianClassdef);
                    lDefRef = aClassdefs.size() -1;
                }
                lBuilder.append(":'").append(hessianClassdef.getType().getValue()).append("'");
                lBuilder.append("#").append(lDefRef);
            }
            lBuilder.append(">");
            for(int i = 0; i < fields.size(); i++)
            {
                String lSubIndent = aIndent + "    ";
                lBuilder.append("\n");
                String lSub = fields.get(i).prettyPrint(lSubIndent, aClassdefs, aObjects);
                lBuilder.append(aIndent);
                lBuilder.append("[").append(i).append("] ").append(lSub.substring(lSubIndent.length()));
            }
            return lBuilder.toString();
        }
    }
}