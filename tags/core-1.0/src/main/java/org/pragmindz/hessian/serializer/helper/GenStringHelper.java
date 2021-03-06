package org.pragmindz.hessian.serializer.helper;
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
public abstract class GenStringHelper
extends GenObjectHelper
{
    protected GenStringHelper(Class aHelpedClass, String aHelperName)
    {
        super(aHelpedClass, aHelperName, new String[]{"val"}, new Class[]{String.class});
    }

    protected Object fields2object(Object... aFields)
    throws Exception
    {
        return string2object((String) aFields[0]);
    }

    protected void object2fields(Object aValue, Object... aFields)
    throws Exception
    {
        aFields[0] = object2string(aValue);
    }

    protected abstract Object string2object(String aString)
    throws Exception;

    protected abstract String object2string(Object aValue)
    throws Exception;
}
