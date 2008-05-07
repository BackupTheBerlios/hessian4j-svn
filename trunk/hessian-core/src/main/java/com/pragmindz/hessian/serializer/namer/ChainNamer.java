package com.pragmindz.hessian.serializer.namer;
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
import com.pragmindz.hessian.serializer.HessianSerializerException;

public class ChainNamer
implements Namer
{
    private Namer[] namers;

    public ChainNamer(Namer ... aNamerChain)
    {
        namers = aNamerChain;
    }

    public String mapHessian2Java(String aHessianName)
    throws HessianSerializerException
    {
        String lName = aHessianName;
        for(int i = 0; i < namers.length; i++) lName = namers[i].mapHessian2Java(lName);
        return lName;
    }

    public String mapJava2Hessian(String aJavaName)
    throws HessianSerializerException
    {
        String lName = aJavaName;
        for(int i = 0; i < namers.length; i++) lName = namers[i].mapJava2Hessian(lName);
        return lName;
    }
}
