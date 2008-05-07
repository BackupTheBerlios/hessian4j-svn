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
public abstract class GenIntegerHelper
extends GenObjectHelper
{
    protected GenIntegerHelper(Class helpedclass, String helpername)
    {
        super(helpedclass, helpername, new String[]{"val"}, new Class[]{Integer.class});
    }

    protected Object fields2object(Object... aFields)
    throws Exception
    {
        return integer2object((Integer) aFields[0]);
    }

    protected void object2fields(Object aValue, Object... aFields)
    throws Exception
    {
        aFields[0] = object2integer(aValue);
    }

    protected abstract Object integer2object(int aInteger)
    throws Exception;

    protected abstract int object2integer(Object aValue)
    throws Exception;
}