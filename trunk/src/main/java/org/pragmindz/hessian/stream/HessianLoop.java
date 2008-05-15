package org.pragmindz.hessian.stream;
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
import org.pragmindz.hessian.model.HessianValue;
import org.pragmindz.hessian.serializer.HessianSerializer;

import java.io.*;

public class HessianLoop
{
    public static Object loop(Object aValue)
    {
        return loop(aValue, new HessianSerializer());
    }

    public static Object loop(Object aValue, HessianSerializer aSer)
    {
        File lFile = null;

        try
        {
            lFile = File.createTempFile("hessian-loop-", ".hes");

            final HessianOutputStream lOut = new HessianOutputStream(new FileOutputStream(lFile), aSer);
            lOut.writeObject(aValue);
            lOut.close();

            final HessianInputStream lIn = new HessianInputStream(aSer, new FileInputStream(lFile));
            Object lResult = lIn.readObject();
            lIn.close();

            return lResult;
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
            return aValue;
        }
        catch(HessianStreamException e)
        {
            e.printStackTrace();
            return aValue;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return aValue;
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            return aValue;
        }
        finally
        {
            if(lFile != null) lFile.delete();
        }
    }

    public static Object loopSerialize(Object aValue)
    {
        try
        {
            final HessianSerializer lSer = new HessianSerializer();
            HessianValue lHes = lSer.serialize(aValue);

            final Object lResult = lSer.deserialize(lHes);
            return lResult;
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            return aValue;
        }
    }
}
