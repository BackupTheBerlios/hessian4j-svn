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
import java.util.ArrayList;
import java.util.List;

/**
 * Ancestor class of the Hessian model.
 * <br>
 * All Hessian model classes extend from here.
 */
public abstract class HessianValue
{
    public void render(OutputStream aStream) throws HessianRenderException
    {
        render(aStream, new ArrayList<HessianString>(), new ArrayList<HessianClassdef>(), new ArrayList<HessianComplex>());
    }

    /**
     * Renders the <code>HessianValue</code> to the given <code>OutputStream</code>.
     *
     * @param aStream to which to render
     * @param types list to store the type strings for maps and lists
     * @param classDefs list to store the class definitions
     * @param objects list to store the objects
     * @throws HessianRenderException
     */
    public abstract void render(OutputStream aStream, List<HessianString> types, List<HessianClassdef> classDefs, List<HessianComplex> objects)
    throws HessianRenderException;

    public final String prettyPrint()
    {
        ArrayList<HessianClassdef> lClassdefs = new ArrayList<HessianClassdef>();
        ArrayList<HessianComplex> lObjs = new ArrayList<HessianComplex>();
        final String lBody = this.prettyPrint("", lClassdefs, lObjs);
        final StringBuilder lBuilder = new StringBuilder();
        for(HessianClassdef lDef : lClassdefs)
            lBuilder.append(lDef.prettyPrint("", lClassdefs, lObjs)).append("\n\n");
        lBuilder.append(lBody).append("\n");
        return lBuilder.toString();                          
    }

    protected abstract String prettyPrint(String aIndent, List<HessianClassdef> aClassdefs, List<HessianComplex> aObjects);

    public String getTypeString()
    {
        throw new UnsupportedOperationException("Type name should be overridden in model-subclass");
    }
}