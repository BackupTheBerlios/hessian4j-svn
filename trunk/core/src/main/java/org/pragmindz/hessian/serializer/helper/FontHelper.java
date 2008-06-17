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
import java.awt.*;

public class FontHelper
extends GenStringHelper
{
    public FontHelper()
    {
        super(Font.class, "FontHelper");
    }

    protected String object2string(Object aValue) throws Exception
    {
        final Font lFont = (Font) aValue;
        final int lFontStyle = lFont.getStyle();

        String lStyle;
        switch(lFontStyle)
        {
            case Font.BOLD: lStyle =  "BOLD"; break;
            case Font.ITALIC: lStyle = "ITALIC"; break;
            case 3: lStyle = "BOLDITALIC"; break;
            case Font.PLAIN:
            default: lStyle="PLAIN";
        }
        return lFont.getName() + "-"+ lStyle + "-" + lFont.getSize();
    }

    protected Object string2object(String aString) throws Exception
    {
        return Font.decode(aString);
    }
}
