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

import java.util.Map;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Models a map of {@link HessianValue} keys and values.
 * <p>
 * The optional type element describes the type of the map.
 * <code><pre>
 * Grammar
 * map        ::= M type? (value value)* z
 * </code></pre>
 */
public class HessianMap
extends HessianComplex
{
    private Map<HessianValue, HessianValue> data;
    private HessianString type;

    public HessianMap()
    {
        // We choose a linked hashmap because we want to preserve order of the
        // key-value pairs in the parser/renderer. It should mimic the hessian format
        // behavior and not change order. This can be important for accessing references.
        data = new LinkedHashMap<HessianValue, HessianValue>();
    }

    public HessianMap(HessianString type)
    {
        this();
        this.type = type;
    }

    public void put(HessianValue aKey, HessianValue aVal)
    {
        data.put(aKey, aVal);
    }

    public boolean containsKey(HessianValue aKey)
    {
        return data.containsKey(aKey);
    }

    public HessianValue get(HessianValue aKey)
    {
        return data.get(aKey);
    }

    public Iterator<HessianValue> keys()
    {
        return data.keySet().iterator();
    }

    public Iterator<HessianValue> values()
    {
        return data.values().iterator();
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

    public int size()
    {
        return data.size();
    }

    @Override
    public String getTypeString()
    {
        return "map";
    }

    /**
     * Renders a HessianMap.
     * 
     * @see com.pragmindz.hessian.model.HessianMap
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
            final int lValueRef = objects.indexOf(this);
            if (lValueRef != -1)
            {
                renderReference(aStream, lValueRef);
            }
            else
            {
                //Add it to the object refs
                objects.add(this);

                aStream.write('M');

                //Render the type if present
                if (hasTypeString()) renderTypeString(aStream, types, type);                

                //Render the key-value pairs
                for (Map.Entry<HessianValue, HessianValue> lEntry : data.entrySet())
                {
                    lEntry.getKey().render(aStream, types, classDefs, objects);
                    lEntry.getValue().render(aStream, types, classDefs, objects);
                }

                // Write the sentinel.
                aStream.write('z');
            }
        }
        catch (IOException e)
        {
            throw new HessianRenderException(e);
        }
    }

    protected String prettyPrint(String aIndent, List<HessianClassdef> aClassdefs, List<HessianComplex> aObjects)
    {
        if (aObjects.contains(this))
        {
            int lRef = aObjects.indexOf(this);
            return aIndent + "<ref:" + lRef + ">";
        }
        else
        {
            aObjects.add(this);
            final StringBuilder lBuilder = new StringBuilder();
            lBuilder.append(aIndent);
            lBuilder.append("<map").append("@").append(aObjects.size() - 1);
            if (type != null) lBuilder.append(":'").append(type.getValue()).append("'");
            lBuilder.append(">");
            int i = 0;
            for (HessianValue lKey: data.keySet())
            {
                String lSubIndent = aIndent + "    ";
                lBuilder.append("\n");
                String lSubKey = lKey.prettyPrint(lSubIndent, aClassdefs, aObjects);
                String lSubVal = data.get(lKey).prettyPrint(lSubIndent, aClassdefs, aObjects);
                lBuilder.append(aIndent);
                lBuilder.append("[").append(i++).append("]\n");
                lBuilder.append(lSubKey).append("\n").append(lSubVal);
            }
            return lBuilder.toString();
        }
    }
}