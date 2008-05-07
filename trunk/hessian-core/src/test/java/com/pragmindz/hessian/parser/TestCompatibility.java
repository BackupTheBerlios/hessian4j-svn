package com.pragmindz.hessian.parser;
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
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import com.caucho.hessian.io.Hessian2Output;
import com.pragmindz.hessian.model.*;

import java.io.*;
import java.util.Date;
import java.util.Map;
import java.util.LinkedHashMap;

import junit.framework.Assert;

/**
 * Test the parser against the Caucho rendered hessian.
 * This tests the compatibility between implementations.
 */
public class TestCompatibility
{
    File tempFile;

    @Before
    public void setup() throws IOException
    {
      tempFile = File.createTempFile("hesstest", "hes");
    }

    @After
    public void teardown()
    {
        tempFile.delete();
        tempFile = null;
    }

    @Test
    public void testShortStrings()
    throws IOException, HessianParserException
    {
        // Length 0-16.
        // Note: Accented characters do not work.
        //       So probably the original implementation does not write the string correctly.

        testString("");
        //testString("י");
        testString("ai");
        //testString("יטכפב");
        testString("123456789abcdefg");
        testString("1234567890abcdefghijklmnopqrstuvwxyz");
        testString("01234567890123456789012345678901");
    }

    @Test
    public void testLongs()
    throws IOException, HessianParserException
    {
        // Range -0x40000 - 0x3ffff.
        testLong(-0x40000);
        testLong(0x3ffff);

        // Range -8 - 15.
        testLong(-8L);
        testLong(15L);

        // Range -2048 - 2047.
        testLong(-2048);
        testLong(2047);

        // Full range longs.
        testLong(Long.MAX_VALUE);
        testLong(Long.MIN_VALUE);

        // 32 bits long.
        testLong(0xffffffff);
        testLong(0x10ab10ab);
    }

    @Test
    public void testDoubles()
    throws IOException, HessianParserException
    {
        // Special representations.
        testDouble(0.0D);
        testDouble(1.0D);
        
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
    public void testIntegers()
    throws IOException, HessianParserException
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
    public void testBooleans()
    throws IOException, HessianParserException            
    {
        testBoolean(true);
        testBoolean(false);
    }

    @Test
    public void testDates()
    throws IOException, HessianParserException
    {
        testDate(new Date());
    }

    @Test
    public void testLists()
    throws IOException, HessianParserException
    {
        // Short length list.
        int[] lVec = new int[5];
        for(int i = 0; i < 5; i++) lVec[i] = i;
        testList(lVec);

        // Long length list.
        lVec = new int[500];
        for(int i = 0; i < 500; i++) lVec[i] = i;
        testList(lVec);
    }

    @Test
    public void testMaps()
    throws IOException, HessianParserException
    {
        Map<Integer, String> aTestMap = new LinkedHashMap<Integer, String>();
        aTestMap.put(1, "uno");
        aTestMap.put(2, "duo");
        aTestMap.put(3, "tres");
        aTestMap.put(4, "quattuor");
        aTestMap.put(5, "cinque");
        testMap(aTestMap);

        Map lLargeMap = new LinkedHashMap();
        for(int i = 0; i < 1000; i++) lLargeMap.put(i, "value: " + i);
        testMap(lLargeMap);
    }

     static class Car implements Serializable
    {
        String type;
        int version;

        public Car(String type, int version)
        {
            this.type = type;
            this.version = version;
        }
    };

    static class Garage implements Serializable
    {
        Car myCar;

        public Garage(Car myCar)
        {
            this.myCar = myCar;
        }
    }

    @Test
    public void testObjects()
    throws IOException, HessianParserException
    {
        final Hessian2Output originalOut = new Hessian2Output(new FileOutputStream(tempFile));

        Car lSportsCar = new Car("Ferrari", 1);
        Garage lGarage = new Garage(lSportsCar);

        originalOut.writeObject(lSportsCar);
        originalOut.writeObject(lGarage);

        originalOut.close();

        HessianParser lMyParser = new HessianParser(new FileInputStream(tempFile));

        final HessianValue lMyCar = lMyParser.nextValue();
        Assert.assertTrue(lMyCar instanceof HessianObject);

        final HessianValue lMyGarage = lMyParser.nextValue();
        Assert.assertTrue(lMyGarage instanceof HessianObject);
    }

    @Test
    public void testNull()
    throws IOException, HessianParserException
    {
        final Hessian2Output originalOut = new Hessian2Output(new FileOutputStream(tempFile));
        originalOut.writeNull();
        originalOut.close();

        HessianParser lMyParser = new HessianParser(new FileInputStream(tempFile));
        final HessianValue lMyObj = lMyParser.nextValue();
        Assert.assertTrue(lMyObj instanceof HessianNull);       
    }

    @Test
    public void testCall() throws HessianRenderException, HessianParserException, IOException
    {
        final Hessian2Output originalOut = new Hessian2Output(new FileOutputStream(tempFile));
        originalOut.call("set_arg1_arg2",new Object[] {new Integer(10), "FOO"});
        originalOut.close();

        HessianParser lMyParser = new HessianParser(new FileInputStream(tempFile));
        HessianValue lMyval = lMyParser.nextValue();
        Assert.assertTrue(lMyval instanceof HessianCall);
        HessianCall lMyCall = (HessianCall)lMyval;
        final String lMyMethod = lMyCall.getMethod();
        Assert.assertEquals("set_arg1_arg2", lMyMethod);
        Assert.assertEquals(2,lMyCall.getArguments().size());
        Assert.assertTrue(lMyCall.getArgument(0) instanceof HessianInteger);
        Assert.assertTrue(lMyCall.getArgument(1) instanceof HessianString);
        Assert.assertEquals(10,((HessianInteger)lMyCall.getArgument(0)).getValue());
        Assert.assertEquals("FOO",((HessianString)lMyCall.getArgument(1)).getValue());
    }

    @Test
    public void testSuccessReply() throws IOException, HessianParserException
    {
        final Hessian2Output originalOut = new Hessian2Output(new FileOutputStream(tempFile));
        originalOut.startReply();
        originalOut.writeObject(new Car("LADA",2105));
        originalOut.completeReply();
        originalOut.close();

        HessianParser lMyParser = new HessianParser(new FileInputStream(tempFile));
        HessianValue lMyval = lMyParser.nextValue();
        Assert.assertTrue(lMyval instanceof HessianReply);
        HessianSuccessReply lReply = (HessianSuccessReply) lMyval;
        Assert.assertTrue(lReply.getValue() instanceof HessianObject);
        HessianObject lResp = (HessianObject) lReply.getValue();
        Assert.assertEquals("LADA",((HessianString)lResp.getField(0)).getValue());
        Assert.assertEquals(2105,((HessianInteger)lResp.getField(1)).getValue());
    }

    @Test
    public void testFaultReply() throws IOException, HessianParserException
    {
        final Hessian2Output originalOut = new Hessian2Output(new FileOutputStream(tempFile));
        originalOut.startReply();
        originalOut.writeFault("ServiceException","Never drive a BMW !",new Exception("oeleboele"));
        originalOut.completeReply();
        originalOut.close();

        HessianParser lMyParser = new HessianParser(new FileInputStream(tempFile));
        HessianValue lMyval = lMyParser.nextValue();
        Assert.assertTrue(lMyval instanceof HessianReply);
        HessianFaultReply lReply = (HessianFaultReply) lMyval;
        HessianFault lFault = lReply.getFault();
        Assert.assertEquals(HessianFault.FaultCode.ServiceException,lFault.getFaultCode());
        Assert.assertEquals("Never drive a BMW !",lFault.getMessage().getValue());
        Assert.assertEquals("java.lang.Exception",lFault.getException().getHessianClassdef().getType().getValue());
        Assert.assertEquals("oeleboele",((HessianString)lFault.getException().getField(0)).getValue());
    }

    @Test
    public void testBinary()
    throws IOException, HessianParserException
    {
        final Hessian2Output originalOut = new Hessian2Output(new FileOutputStream(tempFile));
        originalOut.writeByteBufferStart();
        originalOut.writeByteBufferPart(new byte[10], 0, 10);
        originalOut.writeByteBufferPart(new byte[11], 0, 11);
        originalOut.writeByteBufferEnd(new byte[200], 0, 200);
        originalOut.close();

        HessianParser lMyParser = new HessianParser(new FileInputStream(tempFile));
        final HessianValue lMyBinary = lMyParser.nextValue();
        Assert.assertTrue(lMyBinary instanceof HessianBinary);        
    }

    @Test
    public void testMessage() throws IOException, HessianParserException
    {
        final Hessian2Output originalOut = new Hessian2Output(new FileOutputStream(tempFile));
        originalOut.startMessage();
        originalOut.writeString("oeleboele");
        originalOut.writeInt(10);
        originalOut.completeMessage();
        originalOut.close();

        HessianParser lMyParser = new HessianParser(new FileInputStream(tempFile));
        final HessianValue lVal = lMyParser.nextValue();
        Assert.assertTrue(lVal instanceof HessianMessage);
        final HessianMessage lMsg = (HessianMessage) lVal;
        Assert.assertEquals(2, lMsg.size());
        Assert.assertTrue(lMsg.get(0) instanceof HessianString);
        Assert.assertTrue(lMsg.get(1) instanceof HessianInteger);
        System.out.println(lMsg.prettyPrint());
    }

    @Test
    public void testEnvelope() throws IOException, HessianParserException
    {
        final Hessian2Output originalOut = new Hessian2Output(new FileOutputStream(tempFile));
        originalOut.startEnvelope("myEnvelope");
        originalOut.writeInt(1);  //1 header
        originalOut.writeString("header-key");
        originalOut.writeInt(10);
        final byte[] lBody = "oeleboele".getBytes("UTF8");
        originalOut.writeBytes(lBody,0,lBody.length);
        originalOut.writeInt(1);  //1 footer
        originalOut.writeString("footer-key");
        originalOut.writeBoolean(false);
        originalOut.completeEnvelope();
        originalOut.close();

        HessianParser lMyParser = new HessianParser(new FileInputStream(tempFile));
        final HessianValue lVal = lMyParser.nextValue();
        Assert.assertTrue(lVal instanceof HessianEnvelope);
        final HessianEnvelope lEnv = (HessianEnvelope) lVal;
        Assert.assertEquals(1, lEnv.size());  //1 chunk
        HessianEnvelopeChunk lChunk = lEnv.get(0);
        Assert.assertEquals(1, lChunk.getHeader().size());
        Assert.assertEquals(1, lChunk.getFooter().size());
        Assert.assertEquals(new String(lBody,"UTF8"),new String(lChunk.getBody().get(0),"UTF8"));
    }

    private void testString(String aString)
    throws IOException, HessianParserException
    {
        final Hessian2Output originalOut = new Hessian2Output(new FileOutputStream(tempFile));
        originalOut.writeString(aString);
        originalOut.close();

        HessianParser lMyParser = new HessianParser(new FileInputStream(tempFile));
        final HessianValue lMyString = lMyParser.nextValue();
        Assert.assertTrue(lMyString instanceof HessianString);
        Assert.assertEquals(aString, ((HessianString) lMyString).getValue());
    }

    private void testLong(long aLong)
    throws IOException, HessianParserException
    {
        final Hessian2Output originalOut = new Hessian2Output(new FileOutputStream(tempFile));
        originalOut.writeLong(aLong);
        originalOut.close();

        HessianParser lMyParser = new HessianParser(new FileInputStream(tempFile));
        final HessianValue lMyLong = lMyParser.nextValue();
        Assert.assertTrue(lMyLong instanceof HessianLong);
        Assert.assertEquals(aLong, ((HessianLong) lMyLong).getValue());
    }

    private void testDouble(double aDouble)
    throws IOException, HessianParserException
    {
        final Hessian2Output originalOut = new Hessian2Output(new FileOutputStream(tempFile));
        originalOut.writeDouble(aDouble);
        originalOut.close();

        HessianParser lMyParser = new HessianParser(new FileInputStream(tempFile));
        final HessianValue lMyDouble = lMyParser.nextValue();
        Assert.assertTrue(lMyDouble instanceof HessianDouble);
        Assert.assertEquals(aDouble, ((HessianDouble) lMyDouble).getValue());
    }

    private void testInteger(int aInt)
    throws IOException, HessianParserException
    {
        final Hessian2Output originalOut = new Hessian2Output(new FileOutputStream(tempFile));
        originalOut.writeInt(aInt);
        originalOut.close();

        HessianParser lMyParser = new HessianParser(new FileInputStream(tempFile));
        final HessianValue lMyInteger = lMyParser.nextValue();
        Assert.assertTrue(lMyInteger instanceof HessianInteger);
        Assert.assertEquals(aInt, ((HessianInteger) lMyInteger).getValue());
    }

    private void testBoolean(boolean aBool)
    throws IOException, HessianParserException
    {
        final Hessian2Output originalOut = new Hessian2Output(new FileOutputStream(tempFile));
        originalOut.writeBoolean(aBool);
        originalOut.close();

        HessianParser lMyParser = new HessianParser(new FileInputStream(tempFile));
        final HessianValue lMyBoolean = lMyParser.nextValue();
        Assert.assertTrue(lMyBoolean instanceof HessianBoolean);
        Assert.assertEquals(aBool, ((HessianBoolean) lMyBoolean).isValue());
    }

    private void testDate(Date aDate)
    throws IOException, HessianParserException
    {
        final Hessian2Output originalOut = new Hessian2Output(new FileOutputStream(tempFile));
        originalOut.writeUTCDate(aDate.getTime());
        originalOut.close();

        HessianParser lMyParser = new HessianParser(new FileInputStream(tempFile));
        final HessianValue lMyDate = lMyParser.nextValue();
        Assert.assertTrue(lMyDate instanceof HessianDate);
        Assert.assertEquals(aDate, ((HessianDate) lMyDate).getValue());
    }

    private void testList(int[] aVec)
    throws IOException, HessianParserException
    {
        final Hessian2Output originalOut = new Hessian2Output(new FileOutputStream(tempFile));
        final String lType = aVec.getClass().toString();

        // First time, a full version with full type.
        originalOut.writeListBegin(aVec.length, lType);
        for(int i = 0; i < aVec.length; i++) originalOut.writeInt(aVec[i]);
        originalOut.writeListEnd();

        // Second time to force a type reference.
        originalOut.writeListBegin(aVec.length, lType);
        for(int i = 0; i < aVec.length; i++) originalOut.writeInt(aVec[i]);
        originalOut.writeListEnd();

        originalOut.close();

        HessianParser lMyParser = new HessianParser(new FileInputStream(tempFile));
        HessianValue lMyList = lMyParser.nextValue();
        Assert.assertTrue(lMyList instanceof HessianList);
        Assert.assertEquals(aVec.length, ((HessianList) lMyList).size());        

        lMyList = lMyParser.nextValue();
        Assert.assertTrue(lMyList instanceof HessianList);
        Assert.assertEquals(aVec.length, ((HessianList) lMyList).size());
    }

    private void testMap(Map<Integer, String> aMap)
    throws IOException, HessianParserException
    {
        final Hessian2Output originalOut = new Hessian2Output(new FileOutputStream(tempFile));
        originalOut.writeMapBegin("oeleboele-type");
        for(int lKey: aMap.keySet())
        {
            originalOut.writeInt(lKey);
            originalOut.writeString(aMap.get(lKey));
        }
        originalOut.writeMapEnd();
        originalOut.close();

        HessianParser lMyParser = new HessianParser(new FileInputStream(tempFile));
        HessianValue lMyMap = lMyParser.nextValue();
        Assert.assertTrue(lMyMap instanceof HessianMap);
        Assert.assertEquals(aMap.size(), ((HessianMap) lMyMap).size());        
    }
}
