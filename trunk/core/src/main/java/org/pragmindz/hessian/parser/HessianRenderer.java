package org.pragmindz.hessian.parser;
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
import org.pragmindz.hessian.model.HessianClassdef;
import org.pragmindz.hessian.model.HessianComplex;
import org.pragmindz.hessian.model.HessianString;
import org.pragmindz.hessian.model.HessianValue;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * The renderer is not thread safe since state info is kept in the object.
 * Each thread should instantiate its own renderer.
 */
public class HessianRenderer
{
    private OutputStream stream;
    
    private List<HessianString> types = new LinkedList<HessianString>();
    private List<HessianClassdef> classDefs = new LinkedList<HessianClassdef>();
    private List<HessianComplex> objects = new LinkedList<HessianComplex>();

    public HessianRenderer(OutputStream stream)
    {
        this.stream = stream;
    }

    public void render(HessianValue aValue)
    throws HessianRenderException
    {
        aValue.render(stream, types, classDefs, objects);
    }
}
