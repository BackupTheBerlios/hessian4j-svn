package org.pragmindz.hessian.serializer.namer;
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
import org.pragmindz.hessian.serializer.HessianSerializerException;

public class WipeHibernateNamer
implements Namer
{
    public String mapHessian2Java(String aHessianName)
    throws HessianSerializerException
    {
        return aHessianName;
    }

    public String mapJava2Hessian(String aJavaName)
    throws HessianSerializerException
    {
        if(aJavaName.equals("net.sf.hibernate.collection.List")) return "java.util.ArrayList";
        else if(aJavaName.equals("net.sf.hibernate.collection.Map")) return "java.util.Hashmap";
        else if(aJavaName.equals("net.sf.hibernate.collection.SortedMap")) return "java.util.TreeMap";
        else if(aJavaName.equals("net.sf.hibernate.collection.Set")) return "java.util.LinkedHashSet";
        else if(aJavaName.equals("net.sf.hibernate.collection.SortedSet")) return "java.util.TreeSet";
        return aJavaName;
    }
}