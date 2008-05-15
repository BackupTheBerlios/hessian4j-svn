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
public class ClassHelper
extends GenStringHelper
{
    private static final String PREFIX = "primitive:";
    public ClassHelper()
    {
        super(Class.class, "ClassHelper");
    }

    protected String object2string(Object aValue) throws Exception
    {
        final Class lClass = (Class) aValue;
        if(lClass.isPrimitive()) return PREFIX + lClass.getName();
        return lClass.getName();
    }

    protected Object string2object(String aString) throws Exception
    {
        if(aString.startsWith(PREFIX))
        {
            String lType = aString.substring(PREFIX.length());
            if("byte".equals(lType)) return Byte.TYPE;
            else if("char".equals(lType)) return Character.TYPE;
            else if("short".equals(lType)) return Short.TYPE;
            else if("int".equals(lType)) return Integer.TYPE;
            else if("long".equals(lType)) return Long.TYPE;
            else if("boolean".equals(lType)) return Boolean.TYPE;
            else if("float".equals(lType)) return Float.TYPE;
            else if("double".equals(lType)) return Double.TYPE;            
        }
        return Class.forName(aString);
    }
}
