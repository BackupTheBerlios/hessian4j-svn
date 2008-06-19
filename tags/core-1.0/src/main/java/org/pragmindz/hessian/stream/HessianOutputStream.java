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
import org.pragmindz.hessian.parser.HessianRenderer;
import org.pragmindz.hessian.parser.HessianRenderException;
import org.pragmindz.hessian.serializer.HessianSerializer;
import org.pragmindz.hessian.serializer.HessianSerializerException;
import org.pragmindz.hessian.model.HessianValue;

import java.io.OutputStream;
import java.io.IOException;

public class HessianOutputStream
{
    private OutputStream stream;
    private HessianRenderer renderer;
    private HessianSerializer serializer;

    public HessianOutputStream(OutputStream aStream)
    {
        stream = aStream;
        renderer = new HessianRenderer(aStream);
        serializer = new HessianSerializer();
    }

    public HessianOutputStream()
    {
        this(null);
    }

    public HessianOutputStream(HessianRenderer renderer, HessianSerializer serializer, OutputStream stream)
    {
        this.renderer = renderer;
        this.serializer = serializer;
        this.stream = stream;
    }


    public HessianOutputStream(OutputStream stream, HessianSerializer serializer)
    {
        this.stream = stream;
        this.serializer = serializer;
        renderer = new HessianRenderer(stream);
    }

    public void writeObject(Object aObject)
    throws HessianStreamException
    {
        if(stream == null || renderer == null || serializer == null)
            throw new HessianStreamException("HessianOutputStream is not ready.");

        try
        {
            final HessianValue lValue = serializer.serialize(aObject);
            renderer.render(lValue);
        }
        catch(HessianRenderException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new HessianStreamException(e);
        }
        catch(HessianSerializerException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

    public void setStream(OutputStream stream)
    {
        this.stream = stream;
    }
}
