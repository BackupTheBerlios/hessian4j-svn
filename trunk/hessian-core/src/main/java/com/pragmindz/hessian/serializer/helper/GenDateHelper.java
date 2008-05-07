package com.pragmindz.hessian.serializer.helper;
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
import java.util.Date;

public abstract class GenDateHelper
extends GenObjectHelper
{
    protected GenDateHelper(Class helpedclass, String helpername)
    {
        super(helpedclass, helpername, new String[]{"val"}, new Class[]{Date.class});
    }

    protected Object fields2object(Object... aFields)
    throws Exception
    {
        return date2object((Date) aFields[0]);
    }

    protected void object2fields(Object aValue, Object... aFields)
    throws Exception
    {
        aFields[0] = object2date(aValue);
    }

    protected abstract Object date2object(Date aDate)
    throws Exception;

    protected abstract Date object2date(Object aValue)
    throws Exception;
}
