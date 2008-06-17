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
import java.lang.reflect.InvocationTargetException;

public class InvocationTargetExceptionHelper
extends GenObjectHelper
{
    public InvocationTargetExceptionHelper()
    {
        super(InvocationTargetException.class, "InvocationTargetExceptionHelper", new String[]{"cause", "msg", "stack"}, new Class[]{Throwable.class, String.class, StackTraceElement[].class});
    }

    protected Object fields2object(Object... aFields)
    throws Exception
    {
        final InvocationTargetException lEx = new InvocationTargetException((Throwable) aFields[0], (String) aFields[1]);
        lEx.setStackTrace((StackTraceElement[]) aFields[2]);
        return lEx; 
    }


    protected void object2fields(Object aValue, Object... aFields)
    throws Exception
    {
        final InvocationTargetException lEx = (InvocationTargetException) aValue;
        aFields[0] = lEx.getCause();
        aFields[1] = lEx.getMessage();
        aFields[2] = lEx.getStackTrace();
    }
}
