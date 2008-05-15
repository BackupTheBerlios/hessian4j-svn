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
import java.util.Locale;

public class LocaleHelper
extends GenObjectHelper
{
    public LocaleHelper()
    {
        super(Locale.class, "LocaleHelper",
                new String[]{"language", "country"}, 
                new Class[]{String.class, String.class});
    }

    protected void object2fields(Object aValue, Object... aFields)
    throws Exception
    {
        Locale lLocale = (Locale) aValue;
        aFields[0] = lLocale.getLanguage();
        aFields[1] = lLocale.getCountry();
    }

    protected Object fields2object(Object... aFields)
    throws Exception
    {
        return new Locale((String) aFields[0], (String) aFields[1]);
    }
}