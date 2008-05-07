package com.pragmindz.hessian.serializer.helper;
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
import com.pragmindz.hessian.model.HessianList;
import com.pragmindz.hessian.model.HessianString;
import com.pragmindz.hessian.model.HessianValue;
import com.pragmindz.hessian.model.HessianClassdef;
import com.pragmindz.hessian.serializer.HessianSerializer;
import com.pragmindz.hessian.serializer.HessianSerializerException;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;

/**
 * It is kind of a pseudo helper, it is not meant to be inserted into the repository (that is why its constructor is disabled).
 * It will be called directly by the serializer when it detects a native java array.
 */
public class ArrayHelper
implements HessianHelper
{
    // Single singleton instance.
    public static final ArrayHelper HELPER = new ArrayHelper();

    // Prevent instantiation.
    protected ArrayHelper()
    {
    }

    public Object deserialize(HessianValue aValue, HessianSerializer aEngine, Map<HessianValue, Object> aPool)
    throws HessianSerializerException
    {
        if(!(aValue instanceof HessianList)) throw new HessianSerializerException(String.format("ArrayHelper error while deserializing. Expected a HessianList instance but received an instance of class: '%1$s'.", aValue.getClass().getName()));
        final HessianList lList = (HessianList) aValue;
        if(lList.getType() == null || lList.getType().getValue() == null) throw new HessianSerializerException("ArrayHelper error while deserializing. Type information is missing from the HessianList and the helper needs this information.");
        final String lType = lList.getType().getValue();
        if(lList.isVariable()) throw new HessianSerializerException("ArrayHelper error while deserializing. This helper only handles lists with predefined length (vector) but recieved a variable list.");
        final int lLen = lList.size();
        final Iterator<HessianValue> lIter = lList.iterator();

         // Construct the sub-element class name.
        String lComponentClassName = "unknown";
        if(lType.startsWith("[L"))
            // Array of objects.
            lComponentClassName = lType.substring(2, lType.length() - 1);
        else
            // Array of array; Array of primitive types.
            lComponentClassName = lType.substring(1);

        if(isPrimitiveArray(lComponentClassName))
        {
            if("I".equals(lComponentClassName))
            {
                int[] lArr = new int[lLen];
                int i = 0;
                while (lIter.hasNext())
                {
                    final HessianValue lVal =  lIter.next();
                    lArr[i] = (Integer) aEngine.deserialize(lVal, aPool);
                    i++;
                }
                return lArr;
            }
            else if("C".equals(lComponentClassName))
            {
                char[] lArr = new char[lLen];
                int i = 0;
                while (lIter.hasNext())
                {
                    final HessianValue lVal =  lIter.next();
                    lArr[i] = (char) ((Integer) aEngine.deserialize(lVal, aPool)).intValue();
                    i++;
                }
                return lArr;
            }
            else if("Z".equals(lComponentClassName))
            {
                boolean[] lArr = new boolean[lLen];
                int i = 0;
                while (lIter.hasNext())
                {
                    final HessianValue lVal =  lIter.next();
                    lArr[i] = (Boolean) aEngine.deserialize(lVal, aPool);
                    i++;
                }
                return lArr;
            }
            else if("S".equals(lComponentClassName))
            {
                short[] lArr = new short[lLen];
                int i = 0;
                while (lIter.hasNext())
                {
                    final HessianValue lVal =  lIter.next();
                    lArr[i] = ((Integer) aEngine.deserialize(lVal, aPool)).shortValue();
                    i++;
                }
                return lArr;
            }
            else if("B".equals(lComponentClassName))
            {
                byte[] lArr = new byte[lLen];
                int i = 0;
                while (lIter.hasNext())
                {
                    final HessianValue lVal =  lIter.next();
                    lArr[i] = ((Integer) aEngine.deserialize(lVal, aPool)).byteValue();
                    i++;
                }
                return lArr;
            }
            else if("J".equals(lComponentClassName))
            {
                long[] lArr = new long[lLen];
                int i = 0;
                while (lIter.hasNext())
                {
                    final HessianValue lVal =  lIter.next();
                    lArr[i] = (Long) aEngine.deserialize(lVal, aPool);
                    i++;
                }
                return lArr;
            }
            else if("F".equals(lComponentClassName))
            {
                float[] lArr = new float[lLen];
                int i = 0;
                while (lIter.hasNext())
                {
                    final HessianValue lVal =  lIter.next();
                    lArr[i] = ((Double) aEngine.deserialize(lVal, aPool)).floatValue();
                    i++;
                }
                return lArr;
            }
            else if("D".equals(lComponentClassName))
            {
                double[] lArr = new double[lLen];
                int i = 0;
                while (lIter.hasNext())
                {
                    final HessianValue lVal =  lIter.next();
                    lArr[i] = (Double) aEngine.deserialize(lVal, aPool);
                    i++;
                }
                return lArr;
            }
            else
            {
                throw new HessianSerializerException(String.format("ArrayHelper error while deserializing. Unknown primitive array type: '%1$s'.", lComponentClassName));
            }
        }
        else
        {
            // First calculate the type of the elements given the
            // type of the array itself. This is simple, just drop
            // a [ prefix to loose a level of indirection.
            String lElementType = "unknown";
            if(lType.startsWith("[L"))
                // Remove a level of arrays.
                lElementType = lType.substring(2, lType.length() - 1);
            else if(lType.startsWith("["))
                // We are at the root level and dealing with
                // an object type here. No need to remove a level.
                lElementType = lType.substring(1);
            else throw new HessianSerializerException(String.format("ArrayHelper error while deserializing. Unknown Java array type: '%1$s'.", lType));

            try
            {
                final Class lComponentClass = Class.forName(lElementType);
                final Object lArr = Array.newInstance(lComponentClass, lLen);
                int i = 0;
                while(lIter.hasNext())
                {
                    final Object lObj = aEngine.deserialize(lIter.next(), aPool);
                    Array.set(lArr, i, (Object) lObj);
                    i++;
                }
                return lArr;
            }
            catch(ClassNotFoundException e)
            {
                throw new HessianSerializerException(String.format("ArrayHelper error while deserializing. Trying to deserialize an array of JavaObjects: '%1$s'.", lElementType), e);
            }
        }
    }

    public HessianValue serialize(Object aJavaObject, HessianSerializer aEngine, Map<Object, HessianValue> aObjectPool, Map<Class, HessianClassdef> aClassdefPool) throws HessianSerializerException
    {
        // Gather some general information about the array.
        final Class lJavaClass= aJavaObject.getClass();
        if(!lJavaClass.isArray())  throw new HessianSerializerException(String.format("ArrayHelper error while serializing. Expected a Java array but received an instance of class: '%1$s'.", aJavaObject));
        final String lType = lJavaClass.getName();

        // Represent arrays as lists.
        final HessianList lResult = new HessianList(new HessianString(lType));
        // Fixed length, ie. a vector.
        lResult.setVariable(false);
        // Add to pool for recursion.
        aObjectPool.put(aJavaObject, lResult);

        // Construct the sub-element class name.
        String lComponentClassName = "unknown";
        if(lType.startsWith("[L"))
            // Array of objects.
            lComponentClassName = lType.substring(2, lType.length() - 1);
        else
            // Array of array; Array of primitive types.
            lComponentClassName = lType.substring(1);

        // Now we have to deal with the array elements.
        // The problem is that we have to deal with arrays with primitive values,
        // and there is no generic way to deal with this kind of array.
        // So we have to cast to the correct primitive type.

        if(isPrimitiveArray(lComponentClassName))
        {
            if("I".equals(lComponentClassName))
            {
                int[] lArr = (int[]) aJavaObject;
                for(int i = 0; i < lArr.length; i++) lResult.add(aEngine.serialize(lArr[i], aObjectPool, aClassdefPool));
            }
            else if("C".equals(lComponentClassName))
            {
                char[] lArr = (char[]) aJavaObject;
                // Note: we represent chars as ints in arrays for efficiency.
                for(int i = 0; i < lArr.length; i++) lResult.add(aEngine.serialize((int)lArr[i], aObjectPool, aClassdefPool));
            }
            else if("Z".equals(lComponentClassName))
            {
                boolean[] lArr = (boolean[]) aJavaObject;
                for(int i = 0; i < lArr.length; i++) lResult.add(aEngine.serialize(lArr[i], aObjectPool, aClassdefPool));
            }
            else if("S".equals(lComponentClassName))
            {
                short[] lArr = (short[]) aJavaObject;
                // Note: we represent shorts as ints in arrays for efficiency.
                for(int i = 0; i < lArr.length; i++) lResult.add(aEngine.serialize((int) lArr[i], aObjectPool, aClassdefPool));
            }
            else if("B".equals(lComponentClassName))
            {
                byte[] lArr = (byte[]) aJavaObject;
                for(int i = 0; i < lArr.length; i++) lResult.add(aEngine.serialize((int)lArr[i], aObjectPool, aClassdefPool));
            }
            else if("J".equals(lComponentClassName))
            {
                long[] lArr = (long[]) aJavaObject;
                for(int i = 0; i < lArr.length; i++) lResult.add(aEngine.serialize(lArr[i], aObjectPool, aClassdefPool));
            }
            else if("F".equals(lComponentClassName))
            {
                float[] lArr = (float[]) aJavaObject;
                for(int i = 0; i < lArr.length; i++) lResult.add(aEngine.serialize((double) lArr[i], aObjectPool, aClassdefPool));
            }
            else if("D".equals(lComponentClassName))
            {
                double[] lArr = (double[]) aJavaObject;
                for(int i = 0; i < lArr.length; i++) lResult.add(aEngine.serialize(lArr[i], aObjectPool, aClassdefPool));
            }
            else
            {
                throw new HessianSerializerException(String.format("ArrayHelper error while serializing. Unknown primitive array type: '%1$s'.", lComponentClassName));
            }
        }
        else
        {
            Object[] lArr = (Object[]) aJavaObject;
            for(int i = 0; i < lArr.length; i++) lResult.add(aEngine.serialize(lArr[i], aObjectPool, aClassdefPool));
        }      

        return lResult;
    }

    public Class getHelpedClass()
    {
        return null;
    }

    private boolean isPrimitiveArray(String aClassName)
    {
        return ("I".equals(aClassName) || "Z".equals(aClassName) || "S".equals(aClassName) ||
                "B".equals(aClassName) || "J".equals(aClassName) || "F".equals(aClassName) ||
                "D".equals(aClassName) || "C".equals(aClassName));
    }
}