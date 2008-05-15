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
import org.pragmindz.hessian.serializer.HessianSerializer;
import org.pragmindz.hessian.serializer.HessianSerializerException;
import org.pragmindz.hessian.parser.HessianParser;
import org.pragmindz.hessian.parser.HessianParserException;
import org.pragmindz.hessian.model.HessianValue;

import java.io.InputStream;
import java.io.IOException;

public class HessianInputStream
{
    private InputStream stream;
    private HessianParser parser;
    private HessianSerializer serializer;

    public HessianInputStream(InputStream aStream)
    {
        stream = aStream;
        parser = new HessianParser(aStream);
        serializer = new HessianSerializer();
    }

    public HessianInputStream()
    {
        this(null);
    }

    public HessianInputStream(HessianSerializer serializer, InputStream stream)
    {
        this.serializer = serializer;
        this.stream = stream;
        parser = new HessianParser(stream);
    }

    public Object readObject()
    throws HessianStreamException
    {
        if(stream == null || parser == null || serializer == null)
            throw new HessianStreamException("HessianInputStream is not ready.");

        try
        {
            final HessianValue lValue = parser.nextValue();
            final Object lResult = serializer.deserialize(lValue);
            return lResult;
        }
        catch(HessianSerializerException e)
        {
            e.printStackTrace();
            throw new HessianStreamException(e);
        }
        catch(HessianParserException e)
        {
            e.printStackTrace();
            throw new HessianStreamException(e);
        }
    }

    public void close()
    {
        try
        {
            stream.close();
        }
        catch(IOException e)
        {
            // Ignore.
        }
        finally
        {
            stream = null;
        }
    }

    public HessianSerializer getSerializer()
    {
        return serializer;
    }

    public void setStream(InputStream stream)
    {
        this.stream = stream;
    }
}
