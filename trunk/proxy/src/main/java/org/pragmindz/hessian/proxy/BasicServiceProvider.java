package org.pragmindz.hessian.proxy;
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
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BasicServiceProvider implements IServiceProvider
{
    final private Map<String,Object> repo = new HashMap<String,Object>();
    final private Map<String, String> services = new HashMap<String, String>();

    public BasicServiceProvider() throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        final Document lDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.getClass().getResourceAsStream("/hessian-services.xml"));
        final NodeList lServices = lDoc.getElementsByTagName("service");
        for (int i=0; i< lServices.getLength(); i++)
        {
            Node lService = lServices.item(i);
            final NamedNodeMap lMap = lService.getAttributes();
            final Node lName = lMap.getNamedItem("name");
            final Node lImpl = lMap.getNamedItem("impl");
            final Node lSingle = lMap.getNamedItem("singleton");
            if (lName != null && lImpl != null && lName.getNodeValue().length()>0 && lImpl.getNodeValue().length()>0)
                services.put(lName.getNodeValue(), lImpl.getNodeValue());
                if (lSingle != null && "true".equalsIgnoreCase(lSingle.getNodeValue()))
                {
                    //Let's pre-instantiate the service and put him in the repo
                    Object lSrv = Class.forName(lImpl.getNodeValue()).newInstance();
                    repo.put(lName.getNodeValue(), lSrv);
                }
        }
    }

    public Object getService(String aServiceName) throws ServiceNotFound
    {
        //Let's check for the existence of the service in the repo
        if (repo.containsKey(aServiceName))
            return repo.get(aServiceName);
        else
        {
            //Instantiate the service
            try
            {
                final String lSrv = services.get(aServiceName);
                if (lSrv != null)
                    return Class.forName(lSrv).newInstance();
                else
                    throw new ServiceNotFound();
            }
            catch (InstantiationException e)
            {
                throw new ServiceNotFound();
            }
            catch (IllegalAccessException e)
            {
                throw new ServiceNotFound();
            }
            catch (ClassNotFoundException e)
            {
                throw new ServiceNotFound();
            }
        }
    }
}
