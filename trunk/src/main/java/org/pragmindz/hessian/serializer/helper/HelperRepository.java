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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class HelperRepository<T extends Helper>
{
    private HelperTreeNode<T> root;

    private static class HelperTreeNode<T extends Helper>
    {
        private T helper;
        private List<HelperTreeNode<T>> children;

        public HelperTreeNode(T aClass)
        {
            helper = aClass;
            children = new LinkedList<HelperTreeNode<T>>();
        }

        public T getHelper()
        {
            return helper;
        }

        public boolean insertNode(HelperTreeNode<T> aNode)
        {
            if(aNode.getHelper().getHelpedClass() == helper.getHelpedClass())
            {
                // If the new node supports a class that is equal to the
                // current class, we replace the current class.
                // So if more then one helper is inserted that support the same
                // class, only the last one is kept.
                helper = aNode.getHelper();
                return true;
            }
            else if(helper.getHelpedClass().isAssignableFrom(aNode.getHelper().getHelpedClass()))
            {
                // The helper is a superclass of the new node.
                // This means that the helper should be inserted
                // somewhere below the current node.
                // We first visit the children to see if we have to add further down to a child.
                boolean insertedToSomeChild = false;
                for (Object aChildren : children)
                {
                    HelperTreeNode<T> lChild = (HelperTreeNode<T>) aChildren;
                    boolean lSuccess = lChild.insertNode(aNode);
                    if (lSuccess)
                    {
                        insertedToSomeChild = true;
                        break;
                    }
                }

                // We did not find a child that is a superclass of the current node.
                // Therefore, we add the node as a new child.
                if(!insertedToSomeChild)
                {
                    // Rebalance tree.
                    final Iterator lIter2 = children.iterator();
                    while(lIter2.hasNext())
                    {
                        final HelperTreeNode<T> lChild = (HelperTreeNode<T>) lIter2.next();
                        if(aNode.getHelper().getHelpedClass().isAssignableFrom(lChild.getHelper().getHelpedClass()))
                        {
                            lIter2.remove();
                            aNode.insertNode(lChild);
                        }
                    }

                    // Add the new balanced tree.
                    children.add(aNode);
                }
                return true;
            }
            else
                return false;
        }

        /** Core finder algorithm
         *
         * @param aClass
         * @return A Helper or null if no applicable helper could be found. We first try to
         * find an exact match, and if it cannot be done, we try to find a mapper for the closest parent class.
         */
        T findHelper(Class aClass)
        {
            // If we have an exact match, we return the helper.
            // This is the perfect case.
            if(helper.getHelpedClass() == aClass) return helper;
            else
            {
                // If we do not have an exact match, we go for the
                // more specific match.
                for (HelperTreeNode<T> lChildNode : children)
                {
                    final T lHelper = lChildNode.findHelper(aClass);
                    if (lHelper != null) return lHelper;
                }
            }

            // If the current helper is not an exact match, and none of the
            // subclasses (finer grained) provide a match, we test if the
            // current helper might be applicable to the more specific class.
            // In this case, we might loose information, but it is better than
            // doing nothing. This case also lets us implement general mappers.
            if(helper.getHelpedClass().isAssignableFrom(aClass)) return helper;
            else return null;
        }

        public String prettyPrint(String aIndent)
        {
            StringBuilder lBld = new StringBuilder(aIndent);
            lBld.append(helper.getHelpedClass().getName());
            for(HelperTreeNode<T> lChild : children)
            {
                lBld.append("\n");
                lBld.append(lChild.prettyPrint(aIndent + "   "));
            }
            return lBld.toString();
        }
    }

    private class RootHelper
    implements Helper
    {
        public Class getHelpedClass()
        {
            return Object.class;
        }
    }

    public HelperRepository()
    {
        root = new HelperTreeNode(new RootHelper());
    }

    /**
     * Add a helper to the repository.
     * @param aHelper
     */
    public void addHelper(T aHelper)
    {
        root.insertNode(new HelperTreeNode(aHelper));
    }

    /**
     * Lookup a helper in the repository.
     * @param aClass The class for which a helper is wanted.
     * @return The corresponding helper. There is always a general fallback helper which uses introspection to
     *         serialize the properties of a JavaBean. This property helper is always returned as a last possibility.
     *         So this method always returns a helper.
     */
    public T findHelper(Class aClass)
    {
        return root.findHelper(aClass);
    }

    public String prettyPrint()
    {
        return root.prettyPrint("");
    }
}
