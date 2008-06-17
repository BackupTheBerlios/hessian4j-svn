package org.pragmindz.hessian.parser;
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
import org.pragmindz.hessian.model.*;
import org.pragmindz.hessian.serializer.HessianSerializer;
import org.pragmindz.hessian.serializer.HessianSerializerException;
import junit.framework.Assert;
import org.junit.Test;

import java.io.*;
import java.util.*;

public class RendererTest
{
    @Test
    public void testIntegers()
    throws IOException, HessianRenderException, HessianParserException
    {
        // Full length ints.
        testInteger(0x40000);
        testInteger(0x50000);

        // Single octet ints.
        testInteger(-16);
        testInteger(47);

        // Two octet ints.
        testInteger(-0x800);
        testInteger(0x7ff);

        // Three octet ints.
        testInteger(-0x40000);
        testInteger(0x3ffff);
    }

    @Test
    public void testLists()
    throws IOException, HessianParserException, HessianRenderException
    {
        // Short length list.
        int[] lVec = new int[5];
        for (int i = 0; i < 5; i++) lVec[i] = i;
        testList(lVec);

        // Long length list.
        lVec = new int[500];
        for (int i = 0; i < 500; i++) lVec[i] = i;
        testList(lVec);
    }

    @Test
    public void testDoubles()
    throws IOException, HessianParserException, HessianRenderException
    {
        // Predefined consants.
        testDouble(1.0D);
        testDouble(0.0D);
        
        // Full length doubles.
        testDouble(0.123456789D);

        // -128.0 - 127.0
        testDouble(-128);
        testDouble(127);

        // Short representation.
        testDouble(-32768.0);
        testDouble(32767.0);

        // Float representation.
        testDouble(Float.MAX_VALUE);
        testDouble(Float.MIN_VALUE);
    }

    @Test
    public void testDates()
    throws IOException, HessianParserException, HessianRenderException
    {
        testDate(new Date());
    }

    @Test
    public void testShortStrings()
    throws IOException, HessianParserException, HessianRenderException
    {
        testString("");
        testString("י");
        testString("ai");
        testString("יטכפב");
        testString("123456789abcdefg");
        testString("1234567890abcdefghijklmnopqrstuvwxyz");
        testString("01234567890123456789012345678901");
    }

    @Test
    public void testChunkedStrings()
    throws HessianRenderException, HessianParserException
    {
        final String lMyString = "012345678901234567890123456789012345678901234567890123456";
        HessianString lString = new HessianString(lMyString);
        lString.setChunkSize(10);
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final HessianRenderer renderer = new HessianRenderer(outStream);
        renderer.render(lString);
        HessianParser lParser = new HessianParser(new ByteArrayInputStream(outStream.toByteArray()));
        HessianValue lValue = lParser.nextValue();
        Assert.assertTrue(lValue instanceof HessianString);
        Assert.assertEquals(lMyString, ((HessianString) lValue).getValue());
    }

    @Test
    public void testMaps()
    throws IOException, HessianParserException, HessianRenderException, HessianSerializerException
    {
        Map<Integer, String> aTestMap = new LinkedHashMap<Integer, String>();
        aTestMap.put(1, "uno");
        aTestMap.put(2, "duo");
        aTestMap.put(3, "tres");
        aTestMap.put(4, "quattuor");
        aTestMap.put(5, "cinque");
        testMap(aTestMap);
        testMapBis(aTestMap);

        Map lMyMap = new HashMap();
        lMyMap.put("mapasval", aTestMap);
        lMyMap.put(aTestMap, "mapaskey");
        testMapBis(lMyMap);

        Map lEmpty = new HashMap();
        testMapBis(lEmpty);

        Map lLargeMap = new LinkedHashMap();
        for(int i = 0; i < 500; i++) lLargeMap.put("key: " + i, "value: " + i);
        testMapBis(lLargeMap);
    }

    @Test
    public void testObjects()
    throws HessianRenderException, HessianParserException, HessianSerializerException
    {
        Car lSportsCar = new Car("Ferrari", 1);
        Garage lGarage = new Garage(lSportsCar);

        HessianClassdef lClassDefCar = new HessianClassdef(new HessianString(lSportsCar.getClass().toString()));
        lClassDefCar.add(new HessianString("type"));
        lClassDefCar.add(new HessianString("version"));

        HessianObject lObjCar = new HessianObject(lClassDefCar);
        lObjCar.add(new HessianString(lSportsCar.type));
        lObjCar.add(new HessianInteger(lSportsCar.version));

        HessianClassdef lClassDefGar = new HessianClassdef(new HessianString(lGarage.getClass().toString()));
        lClassDefGar.add(new HessianString("car"));

        HessianObject lObjGar = new HessianObject(lClassDefGar);
        lObjGar.add(lObjCar);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        HessianRenderer renderer = new HessianRenderer(outStream);
        renderer.render(lObjCar);
        renderer.render(lObjGar);

        HessianParser lParser = new HessianParser(new ByteArrayInputStream(outStream.toByteArray()));
        final HessianValue lMyCar = lParser.nextValue();
        Assert.assertTrue(lMyCar instanceof HessianObject);
        final HessianValue lMyGarage = lParser.nextValue();
        Assert.assertTrue(lMyGarage instanceof HessianObject);

        // Test for 1-octet object references 0-255.
        ///////////////////////////////////////////////////////////////////////////////////////
        List lSeries1 = new LinkedList();
        List lSeries2 = new LinkedList();
        for(int i = 0; i < 256; i++)
        {
            Car lSomeCar = new Car("Car nr. " + i, i);
            lSeries1.add(lSomeCar);
            lSeries2.add(lSomeCar);
        }
        // Now contains car0 ... car255 car0 ... car255.
        lSeries1.addAll(lSeries2);

        HessianSerializer lSer = new HessianSerializer();
        outStream = new ByteArrayOutputStream();
        renderer = new HessianRenderer(outStream);
        HessianValue lHess = lSer.serialize(lSeries1);
        renderer.render(lHess);

        lParser = new HessianParser(new ByteArrayInputStream(outStream.toByteArray()));
        final List lSeries = (List) lSer.deserialize(lParser.nextValue());
        Assert.assertTrue(lSeries1.size() == lSeries.size());
        for(int i = 0; i < 256; i++) Assert.assertTrue(lSeries1.get(i) == lSeries1.get(256 + i));
    }

    @Test
    public void testCalls()
    throws HessianRenderException, HessianParserException
    {
        HessianCall lCall = new HessianCall("set");
        lCall.addArgument(new HessianInteger(10));
        lCall.addArgument(new HessianString("FOO"));

        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final HessianRenderer renderer = new HessianRenderer(outStream);
        renderer.render(lCall);

        HessianParser lParser = new HessianParser(new ByteArrayInputStream(outStream.toByteArray()));
        HessianValue lMyval = lParser.nextValue();
        Assert.assertTrue(lMyval instanceof HessianCall);
        HessianCall lMyCall = (HessianCall)lMyval;
        final String lMyMethod = lMyCall.getMethod().substring(0,lMyCall.getMethod().indexOf('_'));
        Assert.assertEquals("set", lMyMethod);
        Assert.assertEquals(2,lMyCall.getArguments().size());
        Assert.assertTrue(lMyCall.getArgument(0) instanceof HessianInteger);
        Assert.assertTrue(lMyCall.getArgument(1) instanceof HessianString);
        Assert.assertEquals(10,((HessianInteger)lMyCall.getArgument(0)).getValue());
        Assert.assertEquals("FOO",((HessianString)lMyCall.getArgument(1)).getValue());
    }

    @Test
    public void testShortBinary()
    throws HessianRenderException, HessianParserException
    {
        byte[] lBytes = new byte[5];
        HessianBinary lBin = new HessianBinary();
        lBin.add(lBytes);
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final HessianRenderer renderer = new HessianRenderer(outStream);
        renderer.render(lBin);

        HessianParser lParser = new HessianParser(new ByteArrayInputStream(outStream.toByteArray()));
        HessianValue lMyBin = lParser.nextValue();
        Assert.assertTrue(lMyBin instanceof HessianBinary);
        Assert.assertEquals(1,((HessianBinary)lMyBin).size());
        Assert.assertEquals(lBytes.length,((HessianBinary)lMyBin).get(0).length);
    }

    @Test
    public void testChunkedBinary()
    throws HessianRenderException, HessianParserException
    {
        HessianBinary lBin = new HessianBinary();
        lBin.add(new byte[20]);
        lBin.add(new byte[20]);

        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final HessianRenderer renderer = new HessianRenderer(outStream);
        renderer.render(lBin);

        HessianParser lParser = new HessianParser(new ByteArrayInputStream(outStream.toByteArray()));
        HessianValue lMyBin = lParser.nextValue();
        Assert.assertTrue(lMyBin instanceof HessianBinary);
        Assert.assertEquals(2,((HessianBinary)lMyBin).size());
        Assert.assertEquals(20,((HessianBinary)lMyBin).get(0).length);
        Assert.assertEquals(20,((HessianBinary)lMyBin).get(1).length);
      }

    @Test
    public void testMessage()
    throws HessianRenderException, HessianParserException
    {
        HessianMessage lMsg = new HessianMessage();
        lMsg.addValue(new HessianString("oeleboele"));
        lMsg.addValue(new HessianInteger(10));

        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final HessianRenderer renderer = new HessianRenderer(outStream);
        renderer.render(lMsg);

        HessianParser lParser = new HessianParser(new ByteArrayInputStream(outStream.toByteArray()));
        HessianValue lVal = lParser.nextValue();
        Assert.assertTrue(lVal instanceof HessianMessage);
        HessianMessage lMyMsg = (HessianMessage) lVal;
        Assert.assertEquals(2, lMyMsg.size());
        Assert.assertTrue(lMyMsg.get(0) instanceof HessianString);
        Assert.assertTrue(lMyMsg.get(1) instanceof HessianInteger);
    }

    @Test
    public void testEnvelope()
    throws UnsupportedEncodingException, HessianRenderException, HessianParserException
    {
        HessianEnvelope lEnv = new HessianEnvelope("myEnvelope");
        Map lHeaders = new HashMap();
        lHeaders.put(new HessianString("key1"),new HessianInteger(10));
        lHeaders.put(new HessianString("key2"),new HessianString("2ndHeaderValue"));
        Map lFooters = new HashMap();
        lFooters.put(new HessianString("key1"), new HessianInteger(20));
        HessianBinary lBody = new HessianBinary();
        lBody.add("This is the envelope body".getBytes("UTF8"));
        lEnv.add(new HessianEnvelopeChunk(lBody, lHeaders, lFooters));

        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final HessianRenderer renderer = new HessianRenderer(outStream);
        renderer.render(lEnv);

        HessianParser lParser = new HessianParser(new ByteArrayInputStream(outStream.toByteArray()));
        HessianValue lMyVal = lParser.nextValue();
        Assert.assertTrue(lMyVal instanceof HessianEnvelope);
        HessianEnvelope lMyEnv = (HessianEnvelope) lMyVal;
        Assert.assertEquals(1,lMyEnv.getChunks().size());
        HessianEnvelopeChunk lMyChunk = lEnv.get(0);
        Assert.assertEquals(2,lMyChunk.getHeader().size());
        Assert.assertEquals(1,lMyChunk.getFooter().size());
        Assert.assertEquals("This is the envelope body",new String(lMyChunk.getBody().get(0),"UTF8"));
    }

    private void testMap(Map<Integer, String> aMap)
    throws IOException, HessianParserException, HessianRenderException
    {
        final HessianMap lMap = new HessianMap(new HessianString(aMap.getClass().toString()));
        for (Map.Entry<Integer, String> lEntry : aMap.entrySet())
        {
            lMap.put(new HessianInteger(lEntry.getKey()), new HessianString(lEntry.getValue()));
        }
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final HessianRenderer renderer = new HessianRenderer(outStream);
        renderer.render(lMap);

        HessianParser lParser = new HessianParser(new ByteArrayInputStream(outStream.toByteArray()));
        HessianValue lMyMap = lParser.nextValue();
        Assert.assertTrue(lMyMap instanceof HessianMap);
        Assert.assertEquals(aMap.size(), ((HessianMap) lMyMap).size());
    }

    private void testMapBis(Map aMap)
    throws IOException, HessianParserException, HessianRenderException, HessianSerializerException
    {
        final HessianSerializer lSer = new HessianSerializer();
        HessianValue lVal = lSer.serialize(aMap);
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final HessianRenderer renderer = new HessianRenderer(outStream);
        renderer.render(lVal);

        HessianParser lParser = new HessianParser(new ByteArrayInputStream(outStream.toByteArray()));
        HessianValue lMyMap = lParser.nextValue();
        Assert.assertTrue(lMyMap instanceof HessianMap);
        Assert.assertEquals(aMap.size(), ((HessianMap) lMyMap).size());

    }

    private void testString(String aString)
    throws HessianRenderException, HessianParserException, IOException
    {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final HessianRenderer renderer = new HessianRenderer(outStream);
        renderer.render(new HessianString(aString));
        HessianParser lParser = new HessianParser(new ByteArrayInputStream(outStream.toByteArray()));
        HessianValue lValue = lParser.nextValue();
        Assert.assertTrue(lValue instanceof HessianString);
        Assert.assertEquals(aString, ((HessianString) lValue).getValue());
    }

    private void testDate(Date aDate)
    throws HessianParserException, HessianRenderException
    {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final HessianRenderer renderer = new HessianRenderer(outStream);
        renderer.render(new HessianDate(aDate));
        HessianParser lParser = new HessianParser(new ByteArrayInputStream(outStream.toByteArray()));
        HessianValue lValue = lParser.nextValue();
        Assert.assertTrue(lValue instanceof HessianDate);
        Assert.assertEquals(aDate, ((HessianDate)lValue).getValue());
    }

    private void testDouble(double aDouble)
    throws HessianRenderException, HessianParserException
    {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final HessianRenderer renderer = new HessianRenderer(outStream);
        renderer.render(new HessianDouble(aDouble));
        HessianParser lParser = new HessianParser(new ByteArrayInputStream(outStream.toByteArray()));
        HessianValue lValue = lParser.nextValue();
        Assert.assertTrue(lValue instanceof HessianDouble);
        Assert.assertEquals(aDouble, ((HessianDouble)lValue).getValue());
    }

    private void testList(int[] aList)
    throws HessianRenderException, IOException, HessianParserException
    {
        // Variable length lists.
        {
            final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            final HessianRenderer renderer = new HessianRenderer(outStream);

            HessianList myList = new HessianList(new HessianString(aList.getClass().toString()));
            for (int i : aList)
            {
                myList.add(new HessianInteger(i));
            }
            renderer.render(myList);

            //Render it a 2nd time to check the type and object reference mechanism
            renderer.render(myList);

            outStream.flush();

            HessianParser lParser = new HessianParser(new ByteArrayInputStream(outStream.toByteArray()));
            HessianValue lMyList = lParser.nextValue();
            Assert.assertTrue(lMyList instanceof HessianList);
            Assert.assertEquals(aList.length, ((HessianList) lMyList).size());

            HessianValue lMyList2 = lParser.nextValue();
            Assert.assertTrue(lMyList2 instanceof HessianList);
            Assert.assertEquals(aList.length, ((HessianList) lMyList2).size());

            Assert.assertSame(lMyList, lMyList2);
        }

        // Do the same for non-variable lists.
        {
            final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            final HessianRenderer renderer = new HessianRenderer(outStream);

            // The main list.
            HessianList myList = new HessianList(new HessianString(aList.getClass().toString()));
            // Add a second list to test list type refs.
            HessianList myList2 = new HessianList(new HessianString(aList.getClass().toString()));
            myList.setVariable(false);
            myList2.setVariable(false);
            for (int i : aList)
            {
                myList.add(new HessianInteger(i));
                myList2.add(new HessianInteger(i));
            }
            renderer.render(myList);
            //Render it a 2nd time to check the type and object reference mechanism
            renderer.render(myList);
            // List with same type to see if the type ref works for lists.
            renderer.render(myList2);           

            outStream.flush();

            HessianParser lParser = new HessianParser(new ByteArrayInputStream(outStream.toByteArray()));
            HessianValue lMyList = lParser.nextValue();
            Assert.assertTrue(lMyList instanceof HessianList);
            Assert.assertEquals(aList.length, ((HessianList) lMyList).size());

            HessianValue lMyList2 = lParser.nextValue();
            Assert.assertTrue(lMyList2 instanceof HessianList);
            Assert.assertEquals(aList.length, ((HessianList) lMyList2).size());

            Assert.assertSame(lMyList, lMyList2);
        }
    }

    private void testInteger(int aInt)
    throws HessianRenderException, IOException, HessianParserException
    {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final HessianRenderer renderer = new HessianRenderer(outStream);
        renderer.render(new HessianInteger(aInt));
        outStream.flush();

        HessianParser lParser = new HessianParser(new ByteArrayInputStream(outStream.toByteArray()));
        final HessianValue lMyInteger = lParser.nextValue();
        Assert.assertTrue(lMyInteger instanceof HessianInteger);
        Assert.assertEquals(aInt, ((HessianInteger) lMyInteger).getValue());
    }

    public static class Car implements Serializable
    {
        String type;
        int version;

        public Car()
        {
        }

        public Car(String type, int version)
        {
            this.type = type;
            this.version = version;
        }
    }

   static class Garage implements Serializable
   {
       Car myCar;

       public Garage(Car myCar)
       {
           this.myCar = myCar;
       }
   }
}
