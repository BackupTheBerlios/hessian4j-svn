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
import java.util.HashMap;

public class CachingHelperRepository<T extends Helper>
extends HelperRepository
{
    private HashMap<Class, Helper> cache = new HashMap<Class, Helper>();

    public void addHelper(Helper aHelper)
    {
        super.addHelper(aHelper);
        // We have to clear the cache because things might have changed.
        cache.clear();
    }

    public Helper findHelper(Class aClass)
    {
        // First try the cache.
        Helper lHelper = cache.get(aClass);
        // Nope, we have to go further.
        if(lHelper == null)
        {
            // Find it in the tree.
            lHelper = super.findHelper(aClass);
            // Remember the find if it is not null.
            if(lHelper != null) cache.put(aClass, lHelper);
        }
        return lHelper;
    }
}
