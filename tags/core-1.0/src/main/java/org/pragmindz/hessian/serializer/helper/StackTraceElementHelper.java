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
public class StackTraceElementHelper
extends GenObjectHelper
{
    public StackTraceElementHelper()
    {
        super(StackTraceElement.class, "StackTraceElementHelper", new String[]{"declaringClass", "methodName", "fileName", "lineNumber"}, new Class[]{String.class, String.class, String.class, Integer.class});
    }

    protected Object fields2object(Object... aFields)
    throws Exception
    {
        return new StackTraceElement((String) aFields[0], (String) aFields[1], (String) aFields[2] ,(Integer) aFields[3]);
    }

    protected void object2fields(Object aValue, Object... aFields) throws Exception
    {
        StackTraceElement lEl = (StackTraceElement) aValue;
        aFields[0] = lEl.getClassName();
        aFields[1] = lEl.getMethodName();
        aFields[2] = lEl.getFileName();
        aFields[3] = lEl.getLineNumber();
    }
}
