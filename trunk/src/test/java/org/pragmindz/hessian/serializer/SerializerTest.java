package org.pragmindz.hessian.serializer;
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
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.List;

/**
 * Tests of the (de)serializer without other utilities.
 */
public class SerializerTest
{
    private HessianSerializer serializer;

    @Before
    public void setup()
    {
        serializer = new HessianSerializer();
    }

    @After
    public void teardown()
    {
    }

    @Test
    public void testRepoStructure()
    {
        System.out.println(serializer.getRepo().prettyPrint());
    }

    @Test
    public void testStrings()
    {
        gentest("", String.class);
        gentest("oeleboele", String.class);
        gentest("A long string, it should be longer then 31 characters length in order to test what exactly?", String.class);
        gentest("01234567890123456789012345678901", String.class);
    }

    @Test
    public void testBooleans()
    {
        gentest(true, Boolean.class);
        gentest(false, Boolean.class);
    }

    @Test
    public void testDates()
    {
        Date now = new Date();
        gentest(now, Date.class);
        gentest(new java.sql.Date(now.getTime()), java.sql.Date.class);
        gentest(new java.sql.Time(now.getTime()), java.sql.Time.class);
        gentest(new java.sql.Timestamp(now.getTime()), java.sql.Timestamp.class);

    }

    @Test
    public void testIntegers()
    {
        gentest(0, Integer.class);
        gentest(Integer.MAX_VALUE, Integer.class);
        gentest(Integer.MIN_VALUE, Integer.class);
    }

    @Test
    public void testLongs()
    {
        gentest(0l, Long.class);
        gentest(Long.MAX_VALUE, Long.class);
        gentest(Long.MIN_VALUE, Long.class);
    }

    @Test
    public void testDoubles()
    {
        gentest(0.0d, Double.class);
        gentest(Double.MAX_VALUE, Double.class);
        gentest(Double.MIN_VALUE, Double.class);
    }

    @Test
    public void testCollections()
    {
        List lList = new LinkedList();
        for(int i = 0; i < 10; i++) lList.add(new Integer(i));
        testCollection(lList, true);

        List lListOfLists = new ArrayList();
        for(int i = 0; i < 10; i++) lListOfLists.add(lList);
        testCollection(lListOfLists, true);

        Set lSet = new HashSet();
        for(int i = 1000; i < 2000; i++) lSet.add("" + i);
        testCollection(lSet, false);

        List lObjList = new LinkedList();
        for(int i = 0; i < 20; i++) lObjList.add(new A());
        testCollection(lObjList, true);

        List lObjlist2 = new LinkedList();
        A lMyA = new A();
        lMyA.setLink(lObjlist2);
        for(int i = 0; i < 20; i++) lObjlist2.add(lMyA);
        testCollection(lObjlist2, true);
    }

    @Test
    public void testMaps()
    {
        Map lMap = new HashMap();
        lMap.put(new Integer(1), "oele");
        lMap.put(new Integer(2), "boele");
        testMap(lMap);

        Map lLargeMap = new LinkedHashMap();
        for(int i = 0; i < 500; i++) lLargeMap.put("key: " + i, "value: " + i);
        testMap(lLargeMap);

        // TODO add more tests, other element types.
        // recursive lists, directly and indirectly.
    }

    public static class A
    {
        private String field1 = "oele";
        private Integer field2 = 69;
        private Object link = null;

        public static final int OELE = 13;

        public String getField1()
        {
            return field1;
        }

        public void setField1(String aField1)
        {
            field1 = aField1;
        }

        public Integer getField2()
        {
            return field2;
        }

        public void setField2(Integer aField2)
        {
            field2 = aField2;
        }

        public Object getLink()
        {
            return link;
        }

        public void setLink(Object aLink)
        {
            link = aLink;
        }

        public boolean equals(Object o)
        {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;

            final A lA = (A) o;

            if(field1 != null ? !field1.equals(lA.field1) : lA.field1 != null) return false;
            if(field2 != null ? !field2.equals(lA.field2) : lA.field2 != null) return false;

            return true;
        }

        public int hashCode()
        {
            int lresult;
            lresult = (field1 != null ? field1.hashCode() : 0);
            lresult = 31 * lresult + (field2 != null ? field2.hashCode() : 0);
            return lresult;
        }
    }

    public static enum Direction {NORTH, EAST, SOUTH, WEST};

    @Test
    public void testObjects()
    {       
        A lMyA = new A();
        gentest(lMyA, A.class);

        A lMyA2 = new A();
        gentest(lMyA2, A.class);

        // Indirect link.
        lMyA.setLink(lMyA2);
        lMyA2.setLink(lMyA);
        gentest(lMyA, A.class);

        // Direct link.
        lMyA.setLink(lMyA);
        gentest(lMyA, A.class);

        Direction lNorth  = Direction.NORTH;
        gentest(lNorth, Direction.class);
    }

    @Test
    public void testLocales()
    {
        gentest(Locale.UK, Locale.class);
        gentest(Locale.US, Locale.class);
        gentest(Locale.GERMANY, Locale.class);
        gentest(Locale.FRANCE, Locale.class);
        gentest(Locale.CANADA, Locale.class);
    }

     @Test
    public void testArrays()
    {
        int[] lIntArr = new int[]{1,2,3,4,5};
        testArray(lIntArr, true);

        int[][] lIntIntArr = new int[][]{{1, 2, 3}, null, {4, 5, 6}, null, {7, 8, 9}};
        testArray(lIntIntArr, false);

        short[] lShortArr = new short[]{0, 3, 6, 9, 12, 15, 18, 21};
        testArray(lShortArr, true);

        char[] lCharArr = new char[]{'a', 'b', 'c', 'd', 'e'};
        testArray(lCharArr, true);

        boolean[] lBoolArr = new boolean[]{true, false, false, true, true, true};
        testArray(lBoolArr, true);

        byte[] lByteArr = new byte[]{Byte.MAX_VALUE, Byte.MIN_VALUE, 0, 1, 2, 3, 4, 5};
        testArray(lByteArr, true);

        long[] lLongArr = new long[]{1000001L, 1000002L, 1000003L};
        testArray(lLongArr, true);

        float[] lFloatArr = new float[]{Float.MIN_VALUE, Float.MAX_VALUE, 0.0f, 0.1f, 0.2f};
        testArray(lFloatArr, true);

        double[] lDoubleArr = new double[]{Double.MIN_VALUE, Double.MAX_VALUE, 0.0, 0.1, 0.2, 0.3};
        testArray(lDoubleArr, true);

        A[] lMyAArray = new A[]{new A(), new A(), new A()};
        testArray(lMyAArray, false);

        A lMyA = new A();
        lMyA.setLink(lMyA);
        A[] lMyRecursion = new A[]{lMyA, lMyA,lMyA, lMyA};
        testArray(lMyRecursion, false);
    }

    @Test
    public void testBytes()
    {
        gentest(Byte.MIN_VALUE, Byte.class);
        gentest(Byte.MAX_VALUE, Byte.class);
        gentest((byte) 0, Byte.class);
    }

    @Test
    public void testShorts()
    {
        gentest(Short.MIN_VALUE, Short.class);
        gentest(Short.MAX_VALUE, Short.class);
        gentest((short) 0, Short.class);
    }

    @Test
    public void testFloats()
    {
        gentest(Float.MIN_VALUE, Float.class);
        gentest(Float.MAX_VALUE, Float.class);
        gentest(Float.NaN, Float.class);
        gentest(Float.NEGATIVE_INFINITY, Float.class);
        gentest(Float.POSITIVE_INFINITY, Float.class);
        gentest(0.0f, Float.class);
    }

    @Test
    public void testCharacters()
    {
        gentest('a', Character.class);
        gentest('é', Character.class);
        gentest('ü', Character.class);
    }

    @Test
    public void testBigDecimals()
    {
        gentest(new BigDecimal("0.000000100000000001111111144444444444444444444444444444555555555555544444444444444444444444"), BigDecimal.class);
    }

    @Test
    public void testBigIntegers()
    {
        gentest(new BigInteger("1111111111111111111111122222222222222222222222233333333333333333333333333333335555555555555555555555"), BigInteger.class);
        gentest(BigInteger.ONE, BigInteger.class);
        gentest(BigInteger.TEN, BigInteger.class);
        gentest(BigInteger.ZERO, BigInteger.class);
    }

    @Test
    public void testColors()
    {
        gentest(Color.BLACK, Color.class);
        gentest(Color.BLUE, Color.class);
        gentest(Color.CYAN, Color.class);
        gentest(Color.DARK_GRAY, Color.class);
        gentest(Color.GREEN, Color.class);
        gentest(Color.RED, Color.class);
    }

    @Test
    public void testCurrencies()
    {
        gentest(Currency.getInstance("EUR"), Currency.class);
        gentest(Currency.getInstance("GBP"), Currency.class);
        gentest(Currency.getInstance("USD"), Currency.class);
        gentest(Currency.getInstance("SEK"), Currency.class);
    }

    @Test
    public void testStringBuffers()
    {
        gentest(new StringBuffer("oeleboele"), StringBuffer.class, false);
    }

    @Test
    public void testFonts()
    {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] lFonts = ge.getAllFonts();
        for(int i = 0; i < lFonts.length; i++) gentest(lFonts[i], Font.class);
    }

    @Test
    public void testException()
    {
        try
        {
            // Force nullpointer.
            System.getenv("dezevarzitzekernietindeenv").length();
        }
        catch (Throwable ex)
        {
            try
            {
                HessianValue lVal = serializer.serialize(ex);
                Object lJavaObject = serializer.deserialize(lVal);
                Assert.assertTrue(ex.getClass().isAssignableFrom(lJavaObject.getClass()));
            }
            catch (HessianSerializerException e)
            {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }
    }

//    @Test
//    public void testVeryStrange()
//    {
//        try
//        {
//            final int strangeness = 5;
//            HessianValue lVal = new HessianInteger(666);
//            for(int i = 0; i < strangeness; i++)
//                lVal = serializer.serialize(lVal);
//            for(int i = 0; i < strangeness; i++)
//                lVal = (HessianValue) serializer.deserialize(lVal);
//        }
//        catch (HessianSerializerException e)
//        {
//            e.printStackTrace();
//            Assert.fail(e.getMessage());
//        }
//    }

    private void testCollection(Collection aCollection, boolean aTestElementEquality)
    {
        try
        {
            HessianValue lVal = serializer.serialize(aCollection);
            Object lJavaObject = serializer.deserialize(lVal);
            Assert.assertTrue(aCollection.getClass().isAssignableFrom(lJavaObject.getClass()));
            Assert.assertEquals(aCollection.size(), ((Collection)lJavaObject).size());

            if(aTestElementEquality)
            {
                Object[] lSource = aCollection.toArray();
                Object[] lTarget = ((Collection)lJavaObject).toArray();
                for(int i = 0; i < lSource.length; i++) Assert.assertEquals(lSource[i], lTarget[i]);
            }
        }
        catch (HessianSerializerException e)
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    private void testMap(Map aMap)
    {
        try
        {
            // TODO: extend test, compare element equality.
            HessianValue lVal = serializer.serialize(aMap);
            Object lJavaObject = serializer.deserialize(lVal);
            Assert.assertTrue(aMap.getClass().isAssignableFrom(lJavaObject.getClass()));
            Assert.assertEquals(aMap.size(), ((Map)lJavaObject).size());
        }
        catch (HessianSerializerException e)
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    private void testArray(Object aArray, boolean aTestElementEquality)
    {
        try
        {
            Assert.assertTrue(aArray.getClass().isArray());
            HessianValue lVal = serializer.serialize(aArray);
            Object lJavaObject = serializer.deserialize(lVal);
            Assert.assertTrue(lJavaObject.getClass().isArray());
            Assert.assertEquals(Array.getLength(aArray), Array.getLength(lJavaObject));
            Assert.assertEquals(aArray.getClass(), lJavaObject.getClass());
            if(aTestElementEquality)
                for(int i = 0; i < Array.getLength(aArray); i++) Assert.assertEquals(Array.get(aArray, i), Array.get(lJavaObject, i));
            
        }
        catch (HessianSerializerException e)
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    private void gentest(Object aValue, Class aClass)
    {
       gentest(aValue, aClass, true);
    }

    private void gentest(Object aValue, Class aClass, boolean aTestEquality)
    {
        try
        {
            Assert.assertTrue(aClass.isAssignableFrom(aValue.getClass()));
            HessianValue lVal = serializer.serialize(aValue);
            Object lJavaObject = serializer.deserialize(lVal);
            Assert.assertTrue(aClass.isAssignableFrom(lJavaObject.getClass()));
            if(aTestEquality) Assert.assertEquals(aValue, lJavaObject);
        }
        catch (HessianSerializerException e)
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}