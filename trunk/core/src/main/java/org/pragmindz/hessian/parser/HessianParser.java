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

import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * The parser is not thread safe since parsing information (buffer) is stored
 * in the object. Each thread should instantiate its own parser.
 */
public class HessianParser
{
    // Stream information.
    private InputStream stream = null;
    private long pos = 0;

    // Pre-allocated buffers.
    private byte[] buffer = new byte[256];

    // Reference information.
    private List<HessianString> types = new LinkedList<HessianString>();
    private List<HessianClassdef> classdefs = new LinkedList<HessianClassdef>();
    private List<HessianComplex> objects = new LinkedList<HessianComplex>();

    public HessianParser(InputStream aStream)
    {
        stream = aStream;
        pos = 0;
    }

    public HessianValue nextValue()
    throws HessianParserException
    {
        if (stream == null) throw new HessianParserException("Stream should not be null.");

        int lNrObjectsAtStart = objects.size();
        long lPosAtStart = pos;

        try
        {
            return nextValue(lookahead());
        }
        catch(HessianParserException e)
        {
            String lMsg = String.format("Error in stream at position: %1$d while parsing a value.", lPosAtStart);
            if(objects.size() > lNrObjectsAtStart) lMsg = String.format("Error in stream at position: %1$d while parsing a hessian value with (incomplete) layout:\n%2$s", lPosAtStart, objects.get(lNrObjectsAtStart).prettyPrint());
            throw new HessianParserException(lMsg, e);
        }
    }

    private int lookahead()
    throws HessianParserException
    {
        return lookahead("hessian control byte");
    }

    private int lookahead(String aWhat)
    throws HessianParserException
    {
        readBuffer(1, aWhat);
        return (buffer[0] & 0xff);
    }

    private HessianValue nextValue(int aLook)
    throws HessianParserException
    {
        HessianValue lResult;
        do
        {
            lResult = null;
            switch (aLook)
            {
                case 0x00: case 0x01: case 0x02: case 0x03: case 0x04: case 0x05: case 0x06: case 0x07:
                case 0x08: case 0x09: case 0x0a: case 0x0b: case 0x0c: case 0x0d: case 0x0e: case 0x0f:
                case 0x10: case 0x11: case 0x12: case 0x13: case 0x14: case 0x15: case 0x16: case 0x17:
                case 0x18: case 0x19: case 0x1a: case 0x1b: case 0x1c: case 0x1d: case 0x1e: case 0x1f:
                {
                    final long lStartPos = pos - 1;
                    try
                    {
                        // utf-8 string length 0-31.
                        if (aLook > 0) readBuffer(aLook, "compact string (length 0-31)");
                        final String lVal = new String(buffer, 0, aLook, "UTF-8");
                        lResult = new HessianString(lVal);
                        break;
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        throw new HessianParserException(String.format("Error while reading a compact string (length 0-31) starting at position: %1$d.", lStartPos), e);
                    }
                }
                case 0x20: case 0x21: case 0x22: case 0x23: case 0x24: case 0x25: case 0x26: case 0x27:
                case 0x28: case 0x29: case 0x2a: case 0x2b: case 0x2c: case 0x2d: case 0x2e: case 0x2f:
                {
                    // binary data length 0-15.
                    final int lLen = aLook - 0x20;
                    readBuffer(lLen, "compact binary (length 0-15)");
                    // Copy the buffer into a chunk.
                    final byte[] data = new byte[lLen];
                    System.arraycopy(buffer, 0, data, 0, lLen);
                    final HessianBinary lVal = new HessianBinary();
                    lVal.add(data);
                    lResult = lVal;
                    break;
                }
                case 0x30: case 0x31: case 0x32: case 0x33: case 0x34: case 0x35: case 0x36: case 0x37:
                    // reserved.
                    reserved(aLook);
                    break;
                case 0x38: case 0x39: case 0x3a: case 0x3b: case 0x3c: case 0x3d: case 0x3e: case 0x3f:
                {
                    // long from -x40000 to x3ffff.
                    readBuffer(2, "short long (3-octets)");
                    final long lLong = ((aLook - 0x3c) << 16 | (buffer[0] & 0xff) << 8 | (buffer[1] & 0xff));
                    lResult = HessianLong.valueOf(lLong);
                    break;
                }
                case 0x40: case 0x41:
                    // reserved.
                    reserved(aLook);
                    break;
                case 0x42:
                {
                    // 8-bit binary data final chunk ('B').
                    readBuffer(2, "binary chunk length ('B')");
                    int lLen = ((buffer[0] & 0xff) << 8 | (buffer[1] & 0xff));
                    // Secondly we read the data.
                    final HessianBinary lVal = new HessianBinary();
                    lVal.add(readAndAllocateBuffer(lLen, "binary chunk data ('B')"));
                    lResult = lVal;
                    break;
                }
                case 0x43:
                    // reserved ('C' streaming call).
                    reserved(aLook);
                    break;
                case 0x44:
                {
                    // 64-bit IEEE encoded double ('D').
                    readBuffer(8, "IEEE double ('D')");
                    final double lDouble = Double.longBitsToDouble((long) (buffer[0] & 0xff) << 56 | (long) (buffer[1] & 0xff) << 48 | (long) (buffer[2] & 0xff) << 40 | (long) (buffer[3] & 0xff) << 32 | (long) (buffer[4] & 0xff) << 24 | (long) (buffer[5] & 0xff) << 16 | (long) (buffer[6] & 0xff) << 8 | (long) (buffer[7] & 0xff));
                    lResult = new HessianDouble(lDouble);
                    break;
                }
                case 0x45:
                {
                    // reserved ('E' envelope).
                    final long lEnvelopeStart = pos -1;
                    try
                    {
                        // Version check.
                        readAndCheckProtocolVersion("envelope ('E')");
                        //Read method name
                        readBuffer(1, "method name ('m')");
                        int aPeek = buffer[0] & 0xff;

                        HessianString lMethod;
                        if (aPeek == 'm') lMethod = (HessianString) nextValue(aPeek);
                        else throw new HessianParserException(String.format("Expected method name ('m') but found other token: 0x%1$x at position: %2$d.", aPeek, pos - 1));

                        final HessianEnvelope lEnv = new HessianEnvelope(lMethod.getValue());

                        aPeek = lookahead("call end-of-call peek");

                        if (aPeek == 0x7a)
                            throw new HessianParserException(String.format("Expected envelope chunk but found end-of-envelope at position: %1$d.", pos - 1));

                        while (aPeek != 0x7a)
                        {
                            // Envelope headers.
                            final Map<HessianString, HessianValue> lHeaders = new HashMap<HessianString, HessianValue>();
                            final HessianInteger lHeaderCnt = (HessianInteger) nextValue(aPeek);
                            for (int i = 0; i < lHeaderCnt.getValue(); i++)
                            {
                                final long lKeyValStart = pos;
                                final HessianValue lKey = nextValue(lookahead());
                                if(!(lKey instanceof HessianString))
                                    throw new HessianParserException(String.format("Hessian envelope contains a header with a key that is not a string but an instance of: '%1$s' at position: %2$d.", lKey.getClass().getName(), lKeyValStart));
                                final HessianValue lValue = nextValue(lookahead());
                                lHeaders.put((HessianString) lKey, lValue);
                            }

                            // Envelope body.
                            HessianBinary lBody = (HessianBinary) nextValue(lookahead());

                            // Envelope footers.
                            final Map<HessianString, HessianValue> lFooters = new HashMap<HessianString, HessianValue>();
                            final HessianInteger lFooterCnt = (HessianInteger) nextValue(lookahead());
                            for (int i = 0; i < lFooterCnt.getValue(); i++)
                            {
                                final long lKeyValStart = pos;
                                final HessianValue lKey = nextValue(lookahead());
                                if(!(lKey instanceof HessianString))
                                    throw new HessianParserException(String.format("Hessian envelope contains a footer with a key that is not a string but an instance of: '%1$s' at position: %2$d.", lKey.getClass().getName(), lKeyValStart));
                                HessianValue lVal = nextValue(lookahead());
                                lFooters.put((HessianString) lKey, lVal);
                            }

                            // Compose the result from headers, footers and body.
                            lEnv.add(new HessianEnvelopeChunk(lBody, lHeaders, lFooters));
                            aPeek = lookahead("end-of-envelope peek");
                        }
                        lResult = lEnv;
                        break;
                    }
                    catch (HessianParserException e)
                    {
                        // Augment the exception with some more information.
                        throw new HessianParserException(String.format("Error while trying to parse a hessian envelope starting at position: %1$d.", lEnvelopeStart), e);
                    }
                }
                case 0x46:
                    // boolean false ('F').
                    lResult = HessianBoolean.FALSE;
                    break;
                case 0x47:
                    // reserved.
                    reserved(aLook);
                    break;
                case 0x48:
                    // reserved ('H' header).
                    reserved(aLook);
                    break;
                case 0x49:
                {
                    // 32-bit signed integer ('I').
                    readBuffer(4, "int ('I')");
                    final int lInt = ((buffer[0] & 0xff) << 24) | ((buffer[1] & 0xff) << 16) | ((buffer[2] & 0xff) << 8) | (buffer[3] & 0xff);
                    lResult = HessianInteger.valueOf(lInt);
                    break;
                }
                case 0x4a:
                {
                    // reference to 1-256th map/list/object.
                    final long lStartRef = pos - 1;
                    readBuffer(1, "short reference (1 octet)");
                    final int lRefVal2 = (buffer[0] & 0xff);

                    // Check if reference exists.
                    if (objects.size() <= lRefVal2 || lRefVal2 < 0)
                        throw new HessianParserException(String.format("Dangling reference (1 octet ref) at position: %1$d. Received ref: %2$d, but there are only %3$d objects in the pool.", lStartRef, lRefVal2, objects.size()));

                    // Pick up the object.
                    lResult = objects.get(lRefVal2);
                    break;
                }
                case 0x4b:
                {
                    // reference to 1-65536th map/list/object.
                    final long lStartRef = pos - 1;
                    readBuffer(2, "short reference (2 octets)");
                    final int lRefVal2 = ((buffer[0] & 0xff) << 8 | (buffer[1] & 0xff));

                    // Check if reference exists.
                    if (objects.size() <= lRefVal2 || lRefVal2 < 0)
                        throw new HessianParserException(String.format("Dangling reference (2 octets ref) at pos %1$d. Received: %2$d, but there are only %3$d objects in the pool.", lStartRef, lRefVal2, objects.size()));

                    // Pick up the object.
                    lResult = objects.get(lRefVal2);
                    break;
                }
                case 0x4c:
                {
                    // 64-bit signed long integer ('L').
                    readBuffer(8, "long ('L')");
                    final long lLong = ((long) (buffer[0] & 0xff) << 56 | (long) (buffer[1] & 0xff) << 48 | (long) (buffer[2] & 0xff) << 40 | (long) (buffer[3] & 0xff) << 32 | (long) (buffer[4] & 0xff) << 24 | (long) (buffer[5] & 0xff) << 16 | (long) (buffer[6] & 0xff) << 8 | (long) (buffer[7] & 0xff));
                    lResult = HessianLong.valueOf(lLong);
                    break;
                }
                case 0x4d:
                {
                    // map with optional type ('M').
                    final long lMapStart = pos - 1;
                    try
                    {
                        // Create the new map object.
                        final HessianMap lVal = new HessianMap();
                        // Remember for refs.
                        objects.add(lVal);

                        HessianString lType = null; // 0x74
                        int aPeek = lookahead("map type");

                        // Try a type string.
                        if (aPeek == 0x74 || aPeek == 0x75)
                        {
                            // Type is present.
                            // We can do direct casting because we know the lookahead and
                            // we know which type we return with this lookahead.
                            lType = (HessianString) nextValue(aPeek);
                            lVal.setType(lType);
                            aPeek = lookahead("end-of-map peek");
                        }

                        int i = 0;
                        while (aPeek != 0x7a)
                        {
                            final long lMapKeyStart = pos -1;
                            final HessianValue lMapKey;
                            try
                            {
                                lMapKey = nextValue(aPeek);
                            }
                            catch(HessianParserException e)
                            {
                                String lTypeName = "<unknown>";
                                if(lType != null && lType.getValue() != null) lTypeName = lType.getValue();
                                lVal.put(new HessianString("***ERROR***"), new HessianString("***NOT YET PARSED***"));
                                throw new HessianParserException(String.format("Error while reading map key with entry index: %1$d for an map of type: '%2$s', starting at position: %3$d.\nThe partial dump of the map:\n%4$s", i, lTypeName, lMapKeyStart, lVal.prettyPrint()), e);
                            }

                            final long lMapValueStart = pos;
                            final HessianValue lMapVal;
                            try
                            {
                                lMapVal = nextValue(lookahead());
                            }
                            catch(HessianParserException e)
                            {
                                String lTypeName = "<unknown>";
                                if(lType != null && lType.getValue() != null) lTypeName = lType.getValue();
                                lVal.put(lMapKey, new HessianString("***ERROR***"));
                                throw new HessianParserException(String.format("Error while reading map value with entry index: %1$d for an map of type: '%2$s', starting at position: %3$d.\nThe partial dump of the map:\n%4$s", i, lTypeName, lMapValueStart, lVal.prettyPrint()), e);                                
                            }

                            lVal.put(lMapKey, lMapVal);
                            aPeek = lookahead("end-of-map peek");
                            i++;
                        }

                        lResult = lVal;
                        break;
                    }
                    catch(HessianParserException e)
                    {
                        // Augment the exception with location information.
                        throw new HessianParserException(String.format("Error while parsing a map ('M') starting at location: %1$d.", lMapStart), e);
                    }
                }
                case 0x4e:
                    // null ('N')
                    lResult = HessianNull.NULL;
                    break;
                case 0x4f:
                {
                    // object definition ('O')
                    final long lStartDef = pos - 1;
                    try
                    {
                        // Read the type string first.
                        final HessianValue lTypeRepr = nextValue(lookahead());
                        if (!(lTypeRepr instanceof HessianString))
                            throw new HessianParserException(String.format("Expected hessian string to indicate the class type. Received an instance of: '%1$s'.", lTypeRepr.getClass().getSimpleName()));
                        final HessianString lType = (HessianString) lTypeRepr;

                        // Read the number of fields.
                        final HessianValue lNrFields = nextValue(lookahead());
                        if (!(lNrFields instanceof HessianInteger))
                            throw new HessianParserException(String.format("Expected hessian integer to indicate the number of fields. Received an instance of: '%1$s'.", lNrFields.getClass().getSimpleName()));
                        int lNrFieldsVal = ((HessianInteger) lNrFields).getValue();

                        // Read all the fieldnames.
                        final HessianClassdef lVal = new HessianClassdef(lType);
                        for (int i = 0; i < lNrFieldsVal; i++)
                        {
                            final HessianValue lFld = nextValue(lookahead());
                            if (!(lFld instanceof HessianString))
                                throw new HessianParserException(String.format("Expected object field name to be a string. Received an instance of: '%1$s'.", lFld.getClass().getSimpleName()));
                            lVal.add((HessianString) lFld);
                        }

                        // We have to remember the classdef for later refs.
                        classdefs.add(lVal);                        
                        lResult = lVal;
                        aLook = lookahead();
                        break;
                    }
                    catch (HessianParserException e)
                    {
                        throw new HessianParserException(String.format("Error while reading a hessian class def ('O') that starts at position: %1$d.", lStartDef), e);
                    }
                }
                case 0x50:
                    // reserved ('P' streaming message/post).
                    reserved(aLook);
                    break;
                case 0x51:
                    // reserved
                    reserved(aLook);
                    break;
                case 0x52:
                {
                    // reference to map/list/object - integer ('R').
                    readBuffer(4, "reference ('R')");
                    final long lRefVal = ((buffer[0] & 0xff) << 24 | (buffer[1] & 0xff) << 16 | (buffer[2] & 0xff) << 8 | (buffer[3] & 0xff));
                    final int lRefVal2 = (int) lRefVal;

                    // Check if reference exists.
                    if (objects.size() <= lRefVal2 || lRefVal2 < 0)
                        throw new HessianParserException(String.format("Dangling reference ('R'). Received ref: %1$d but there are only %2$d objects.", lRefVal2, objects.size()));

                    // Pick up the object.
                    lResult = objects.get(lRefVal2);
                    break;
                }
                case 0x53:
                    // utf-8 string final chunk ('S').
                    lResult = new HessianString(readString("string ('S')"));
                    break;
                case 0x54:
                    // boolean true ('T')
                    lResult = HessianBoolean.TRUE;
                    break;
                case 0x55:
                    // reserved
                    reserved(aLook);
                    break;
                case 0x56:
                {
                    // list/vector ('V').
                    final long lVecStart = pos - 1;
                    try
                    {
                        final HessianList lVal = new HessianList();

                        // Remember for refs.
                        objects.add(lVal);

                        HessianString lType = null; // 0x74
                        int aPeek = lookahead("list type/length");

                        // Try a type string.
                        if (aPeek == 0x74 || aPeek == 0x75)
                        {
                            // Type is present.
                            lType = (HessianString) nextValue(aPeek);
                            lVal.setType(lType);
                            // Try length
                            aPeek = lookahead("list type/length");
                        }

                        // Try a length indicator.
                        long lLen = -1;
                        if (aPeek == 0x6c)
                        {
                            // Length is present.
                            readBuffer(4, "list/vector length ('l')");
                            lLen = ((long) (buffer[0] & 0xff) << 24 | (long) (buffer[1] & 0xff) << 16 | (long) (buffer[2] & 0xff) << 8 | (long) (buffer[3] & 0xff));
                            // Read new lookahead.
                            aPeek = lookahead("list type/length");
                        }
                        else if (aPeek == 0x6e)
                        {
                            lLen = lookahead("compact length");
                            // Read new lookahead.
                            aPeek = lookahead("list type/length");
                        }

                        int i = 0;
                        while (aPeek != 0x7a)
                        {
                            final long lElStart = pos - 1;
                            try
                            {
                                final HessianValue lElement = nextValue(aPeek);
                                lVal.add(lElement);
                                aPeek = lookahead("list end-of-list peek");
                                i++;
                            }
                            catch(HessianParserException e)
                            {
                                // Augment the exception with context information for easy debugging.
                                String lTypeName = "<unknown>";
                                if(lType != null && lType.getValue() != null) lTypeName = lType.getValue();
                                lVal.add(new HessianString("***ERROR***"));
                                throw new HessianParserException(String.format("Error parsing list/vector element with index: %1$d with list type: '%2$s' starting at location: %3$d.\nA partial dump:\n%4$s", i, lTypeName, lElStart, lVal.prettyPrint()), e);
                            }
                        }

                        if (lLen > 0) lVal.setVariable(false);

                        if (lLen >= 0 && lLen != lVal.size())
                            throw new HessianParserException(String.format("Inconsistent list/vector length found. Length field contains: %1$d; elements found: %2$d.", lLen, lVal.size()));

                        lResult = lVal;
                        break;
                    }
                    catch (HessianParserException e)
                    {
                        throw new HessianParserException(String.format("Error while reading a hessian list/vector ('V') that starts at position: %1$d.", lVecStart), e);
                    }
                }
                case 0x57: case 0x58: case 0x59: case 0x5a: case 0x5b: case 0x5c: case 0x5d: case 0x5e:
                case 0x5f: case 0x60: case 0x61:
                    // reserved
                    reserved(aLook);
                    break;
                case 0x62:
                {
                    // 8-bit binary data non-final chunk ('b').
                    final long lChunkStart = pos - 1;
                    try
                    {
                        // First we read the length.
                        readBuffer(2, "binary chunk length ('b')");
                        int lLen = ((buffer[0] & 0xff) << 8 | (buffer[1] & 0xff));

                        // Secondly we read the data.
                        final HessianBinary lVal = new HessianBinary();
                        lVal.add(readAndAllocateBuffer(lLen, "binary chunk data ('b')"));

                        // Check the next element.
                        int aPeek = lookahead("binary chunk control");

                        while (aPeek == 0x62)
                        {
                            readBuffer(2, "binary chunk length ('b')");
                            lLen = ((buffer[0] & 0xff) << 8 | (buffer[1] & 0xff));
                            lVal.add(readAndAllocateBuffer(lLen, "binary chunk data ('b')"));

                            // Check the next element.
                            aPeek = lookahead("binary chunk control");
                        }

                        if (aPeek == 0x42)
                        {
                            readBuffer(2, "binary chunk length ('B')");
                            lLen = ((buffer[0] & 0xff) << 8 | (buffer[1] & 0xff));
                            lVal.add(readAndAllocateBuffer(lLen, "binary chunk data ('B')"));
                        }
                        else if (aPeek >= 0x20 && aPeek <= 0x2f)
                        {                            
                            HessianBinary lSmall = (HessianBinary) nextValue(aPeek);
                            lVal.add(lSmall.get(0));
                        }
                        else
                            throw new HessianParserException(String.format("Expected a final binary chunk ('B') but found other control token: 0x%1$x at position: %2$d.", aPeek, pos));

                        lResult = lVal;
                        break;
                    }
                    catch (HessianParserException e)
                    {
                        // Augment the exception with the details of the surrounding chunk, more specific the start of the
                        // binary chunk so that it is easier to locate the error.
                        throw new HessianParserException(String.format("Error while parsing a binary chunk ('b') that starts at position: %1$d.", lChunkStart), e);
                    }
                }
                case 0x63:
                {
                    // RPC-style call ('c')
                    final long lCallStart = pos - 1;
                    try
                    {
                        readAndCheckProtocolVersion("RPC call ('c')");

                        //Read method name
                        int aPeek = lookahead("method name ('m')");

                        HessianString lMethod;
                        if (aPeek == 'm') lMethod = (HessianString) nextValue(aPeek);
                        else throw new HessianParserException(String.format("Expected method name ('m') but found other token: 0x%1$x at position: %2$d.", aPeek, pos - 1));

                        final HessianCall lCall = new HessianCall(lMethod.getValue());

                        aPeek = lookahead("call end-of-call peek");
                        while (aPeek != 0x7a)
                        {
                            final HessianValue lVal = nextValue(aPeek);
                            lCall.addArgument(lVal);
                            aPeek = lookahead("call end-of-call peek");
                        }

                        lResult = lCall;
                        break;
                    }
                    catch (HessianParserException e)
                    {
                        // Augment the exception with information about the surrounding RPC block, so that
                        // it is easier to close in on the error in the file.
                        throw new HessianParserException(String.format("Error while parsing a RPC call ('c') that starts at position: %1$d.)", lCallStart), e);
                    }
                }
                case 0x64:
                {
                    // UTC time encoded as 64-bit long milliseconds since epoch ('d').
                    readBuffer(8, "UTC date ('d')");
                    final long lLong = ((long) (buffer[0] & 0xff) << 56 | (long) (buffer[1] & 0xff) << 48 | (long) (buffer[2] & 0xff) << 40 | (long) (buffer[3] & 0xff) << 32 | (long) (buffer[4] & 0xff) << 24 | (long) (buffer[5] & 0xff) << 16 | (long) (buffer[6] & 0xff) << 8 | (long) (buffer[7] & 0xff));
                    lResult = new HessianDate(new Date(lLong));
                    break;
                }
                case 0x65:
                    // reserved
                    reserved(aLook);
                    break;
                case 0x66:
                {
                    // reserved ('f' for fault for RPC)
                    final long lFaultPos = pos - 1;
                    try
                    {
                        // Read the fault code.
                        final long lCodeKeyStart = pos;
                        HessianValue lKey = nextValue(lookahead());
                        if (!(lKey instanceof HessianString))
                            throw new HessianParserException(String.format("Expected code-key to be a hessian string. Received an instance of: '%1$s'.", lKey.getClass().getSimpleName()));
                        if (!"code".equals(((HessianString) lKey).getValue()))
                            throw new HessianParserException(String.format("Expected the constant 'code' but received the constant: '%1$s' at position: %2$d.", ((HessianString) lKey).getValue(), lCodeKeyStart));

                        final long lCodeValueStart = pos;
                        HessianValue lFaultCode = nextValue(lookahead());
                        if (!(lFaultCode instanceof HessianString))
                            throw new HessianParserException(String.format("Expected code-value to be a hessian string. Received an instance of: '%1$s' at position: %2$d.", lFaultCode.getClass().getSimpleName(), lCodeValueStart));

                        HessianFault.FaultCode lFault;
                        try
                        {
                            lFault = HessianFault.FaultCode.valueOf(((HessianString) lFaultCode).getValue());
                        }
                        catch (IllegalArgumentException e)
                        {
                            throw new HessianParserException(String.format("Expected one of the following fault-codes : [%1$s] but received: '%2$s' at position: %3$d.", HessianFault.FaultCode.values(), lFaultCode, lCodeValueStart));
                        }

                        // Read the message.
                        final long lMsgKeyStart = pos;
                        lKey = nextValue(lookahead());
                        if (!(lKey instanceof HessianString))
                            throw new HessianParserException(String.format("Expected message-key to be a hessian string. Received an instance of: '%1$s' at position: %2$d.", lKey.getClass().getSimpleName(), lMsgKeyStart));
                        if (!"message".equals(((HessianString) lKey).getValue()))
                            throw new HessianParserException(String.format("Expected the constant 'message' but received the constant: '%1$s' at position: %2$d.", ((HessianString) lKey).getValue(), lMsgKeyStart));

                        final long lMsgValueStart = pos;
                        HessianValue lMessage = nextValue(lookahead());
                        if (!(lMessage instanceof HessianString))
                            throw new HessianParserException(String.format("Expected message-value to be a hessian string. Received an instance of: '%1$s' at position: %2$d.", lMessage.getClass().getSimpleName(), lMsgValueStart ));

                        // Read the exception, if there is one...
                        int aPeek = lookahead("end-of-fault marker 'z'");

                        if (aPeek != 'z')
                        {
                            final long lDetKeyStart = pos;
                            lKey = nextValue(aPeek);
                            if (!(lKey instanceof HessianString))
                                throw new HessianParserException(String.format("Expected detail-key to be a hessian string. Received an instance of: '%1$s' at position: %2$d.", lKey.getClass().getSimpleName(), lDetKeyStart));

                            final long lDetValStart = pos;
                            final HessianValue lExc = nextValue(lookahead());
                            if (!(lExc instanceof HessianObject))
                                throw new HessianParserException(String.format("Expected exception to be a hessian object. Received an instance of: '%1$s' at pos %2$d.", lExc.getClass().getSimpleName(), lDetValStart));

                            lResult = new HessianFault(lFault, (HessianString) lMessage, (HessianObject) lExc);
                        }
                        else
                        {
                            lResult = new HessianFault(lFault, (HessianString) lMessage);
                        }
                        break;
                    }
                    catch (HessianParserException e)
                    {
                        // Augment the exception with information about the surrounding block location, this makes it easier for
                        // the user to locate the position in the stream by gradually closing in on the exact location.
                        throw new HessianParserException(String.format("Error while parsing an RPC fault ('f') that starts at position: %1$d.", lFaultPos), e);
                    }
                }
                case 0x67:
                    // double 0.0
                    lResult = HessianDouble.ZERO;
                    break;
                case 0x68:
                    // double 1.0
                    lResult = HessianDouble.ONE;
                    break;
                case 0x69:
                {
                    // double represented as byte (-128.0 to 127.0).
                    readBuffer(1, "long (1-octet)");
                    lResult = new HessianDouble(buffer[0]);
                    break;
                }
                case 0x6a:
                {
                    // double represented as short (-32768.0 to 327676.0).
                    readBuffer(2, "double (as a short)");
                    final short lShort = (short) ((short) (buffer[0] & 0xff) << 8 | (short) (buffer[1] & 0xff));
                    lResult = new HessianDouble(lShort);
                    break;
                }
                case 0x6b:
                {
                    // double represented as float.
                    readBuffer(4, "double (as a float)");
                    final float lFloat = Float.intBitsToFloat((buffer[0] & 0xff) << 24 | (buffer[1] & 0xff) << 16 | (buffer[2] & 0xff) << 8 | (buffer[3] & 0xff));
                    lResult = new HessianDouble(lFloat);
                    break;
                }
                case 0x6c:
                {
                    // list/vector length ('l').
                    // This code is only read with lookahead while handling lists and maps.
                    // So it is always read explicitly while parsing structure.
                    // If it is encountered while expecting a brand new value, we have a problem.
                    // That is why it is unexpected at this point.
                    reserved(aLook);
                    break;
                }
                case 0x6d:
                {
                    // 'm' b1 b0 <method-string>
                    lResult = new HessianString(readString("method name ('m')"));
                    break;
                }
                case 0x6e:
                {
                    // list/vector compact length.
                    // This code is only read with lookahead while handling lists and maps.
                    // So it is always read explicitly while parsing structure.
                    // If it is encountered while expecting a brand new value, we have a problem.
                    // That is why it is unexpected at this point.
                    reserved(aLook);
                    break;
                }
                case 0x6f:
                {
                    // object instance ('o').
                    final long lObjStart = pos -1;
                    try
                    {
                        // Note see Caucho Mantis issue 0002142.
                        // The spec differs from the reference implementation.

                        // First we get the index of the class.
                        final long lClassdefRefStart = pos;
                        final HessianValue lRef = nextValue(lookahead());
                        if (!(lRef instanceof HessianInteger))
                            throw new HessianParserException(String.format("Expected a hessian integer to point to the classdef of the object. Received an instance of: '%1$s' at position: %2$d.",lRef.getClass().getSimpleName() , lClassdefRefStart));
                        int lRefVal = ((HessianInteger) lRef).getValue();

                        // Fetch the classdef.
                        if (classdefs.size() <= lRefVal)
                            throw new HessianParserException(String.format("Object contains reference to unexisting classdef. Received reference: %1$d at position: %2$d but there are only %3$d classdefs available.", lRefVal, lClassdefRefStart, classdefs.size()));
                        final HessianClassdef lDef = classdefs.get(lRefVal);

                        // Finally we can construct the object.
                        final HessianObject lObj = new HessianObject(lDef);

                        // We must store the object for later reference.
                        objects.add(lObj);

                        for (int i = 0; i < lDef.size(); i++)
                        {
                            final long lFieldPos = pos;

                            try
                            {
                                final HessianValue lVal = nextValue(lookahead());
                                lObj.add(lVal);
                            }
                            catch (HessianParserException e)
                            {
                                // Augment the exception with information about the context
                                // where the error occured so that the user can locate the location and the context
                                // more easily.
                                final String lClassdefName = lDef.getType().getValue();
                                final String lFldName = lDef.getFieldName(i).getValue();
                                lObj.add(new HessianString("***ERROR***"));
                                throw new HessianParserException(String.format("Error while reading field with index: %1$d and name: '%2$s' for an object of hessian class: '%3$s', starting at position: %4$d.\nThe partial dump of the object:\n%5$s", i, lFldName, lClassdefName, lFieldPos, lObj.prettyPrint()), e);
                            }
                        }

                        lResult = lObj;
                        break;
                    }
                    catch (HessianParserException e)
                    {
                        // Augment the exception with information about the start of the surrounding object so that
                        // it is easier for the user to locate the error and its context.
                        throw new HessianParserException(String.format("Error while parsing a hessian object ('o') that starts at position: %1$d", lObjStart), e);
                    }
                }
                case 0x70:
                {
                    //  Message/post ('p' - message/post)
                    readAndCheckProtocolVersion("message/post ('p')");

                    //Check the next element
                    int aPeek = lookahead("message value");

                    HessianMessage lMsg = new HessianMessage();
                    while (aPeek != 'z')
                    {
                        lMsg.addValue(nextValue(aPeek));

                        //Check the next element
                        aPeek = lookahead("message value");
                    }

                    lResult = lMsg;
                    break;
                }
                case 0x71:
                    // reserved
                    reserved(aLook);
                    break;
                case 0x72:
                {
                    // reserved ('r' reply for message/RPC)
                    readAndCheckProtocolVersion("message/RPC reply ('r')");

                    //Check the next element
                    int aPeek = lookahead("reply body peek");

                    if (aPeek == 0x66)
                    {
                        //We have a fault reply
                        final HessianFault lFault = (HessianFault) nextValue(aPeek);
                        lResult = new HessianFaultReply(lFault);
                    }
                    else
                    {
                        //Success reply
                        final HessianValue lValue = nextValue(aPeek);
                        lResult = new HessianSuccessReply(lValue);
                    }
                    break;
                }
                case 0x73:
                {
                    // utf-8 string non-final chunk ('s').
                    final long lChunkStart = pos - 1;
                    try
                    {
                        // Read the chunk contents.
                        final HessianString lVal = new HessianString(readString("non-final string chunk ('s')"));
                        // Check the next element.
                        int aPeek = lookahead("string chunk control");

                        while (aPeek == 0x73 || (aPeek >= 0x00 && aPeek <= 0x1f))
                        {
                            if(aPeek == 0x73)
                            {
                                // Short chunk. Read the contents.
                                lVal.add(readString("string ('s')"));
                                // Check the next element.
                                aPeek = lookahead("string chunk control");
                            }
                            else
                            {
                                // Compact string.
                                final HessianString lChunk = (HessianString) nextValue(aLook);
                                lVal.add(lChunk.getValue());
                            }
                        }

                        // The final chunk must start with a captital 'B'.
                        final long lFinalChunkStart = pos - 1;
                        if (aPeek == 0x53)
                        {
                            // Read the body of the final chunk.
                            lVal.add(readString("string ('S')"));
                        }
                        else
                            throw new HessianParserException(String.format("Expected a final string chunk ('S') but found other control token: 0x%1$x at position: %2$d.", aPeek, lFinalChunkStart));
                        lResult = lVal;
                        break;
                    }
                    catch (HessianParserException e)
                    {
                        // Augment the exception with extra location and context information.
                        throw new HessianParserException(String.format("Error occured while parsing a stream of string chunks starting at position: %1$d.", lChunkStart), e);
                    }
                }
                case 0x74:
                {
                    // map/list type ('t').
                    final HessianString lVal = new HessianString(readString("list/vector type ('t')"));
                    // Add the type to the list of types for later reference.
                    types.add(lVal);
                    lResult = lVal;
                    break;
                }
                case 0x75:
                {
                    // type-ref.
                    final int lRef = lookahead("type ref");
                    checkTypeRef(pos - 1, lRef);
                    lResult = types.get(lRef);
                    break;
                }
                case 0x76:
                {
                    // compact vector ('v').
                    final long lVectorStart = pos - 1;
                    try
                    {
                        final long lVecTypeStart = pos;
                        final HessianValue lRef = nextValue(lookahead());
                        if (!(lRef instanceof HessianInteger))
                            throw new HessianParserException(String.format("Expected compact vector type reference to be a hessian integer. Received an instance of: '%1$s' at position: %2$d.", lRef.getClass().getSimpleName(), lVecTypeStart));

                        final long lVecLenStart = pos;
                        final HessianValue lLen = nextValue(lookahead());
                        if (!(lLen instanceof HessianInteger))
                            throw new HessianParserException(String.format("Expected compact vector length to be a  hessian integer. Received an instance of: '%1$s' at position: %2$d.", lLen.getClass().getSimpleName(), lVecLenStart));

                        final int lRefVal = ((HessianInteger) lRef).getValue();
                        final int lLenVal = ((HessianInteger) lLen).getValue();

                        checkTypeRef(pos - 2, lRefVal);
                        final HessianList lVal = new HessianList(types.get(lRefVal));
                        lVal.setVariable(false);
                        // Remember for refs.
                        objects.add(lVal);

                        for (int i = 0; i < lLenVal; i++)
                        {
                            final long lElPos = pos;
                            try
                            {
                                lVal.add(nextValue(lookahead()));
                            }
                            catch (HessianParserException e)
                            {
                                // Augment the exception with context information.
                                lVal.add(new HessianString("***ERROR***"));
                                throw new HessianParserException(String.format("Error while parsing vector element with index: %1$d starting at position: %2$d.\nA partial list dump:\n%3$s", i, lElPos, lVal.prettyPrint()), e);
                            }
                        }
                        
                        lResult = lVal;
                        break;
                    }
                    catch (HessianParserException e)
                    {
                        // Augment exception with more context info.
                        throw new HessianParserException(String.format("Error while parsing a compact vector ('v') that starts at position: %1$d.", lVectorStart), e);
                    }
                }
                case 0x77:
                {
                    // long encoded as 32-bit int.
                    readBuffer(4, "long (as int)");
                    final long lLong = ((long) (buffer[0] & 0xff) << 24 | (long) (buffer[1] & 0xff) << 16 | (long) (buffer[2] & 0xff) << 8 | (long) (buffer[3] & 0xff));
                    lResult = HessianLong.valueOf(lLong);
                    break;
                }
                case 0x78: case 0x79:
                    // reserved
                    reserved(aLook);
                    break;
                case 0x7a:
                    // list/map terminator ('z').
                    // This code is only read with lookahead while handling lists and other containers.
                    // So it is always read explicitly while parsing structure.
                    // If it is encountered while expecting a brand new value, we have a problem.
                    // That is why it is unexpected at this point.
                    reserved(aLook);
                    break;
                case 0x7b: case 0x7c: case 0x7d: case 0x7e: case 0x7f:
                    // reserved
                    break;
                case 0x80: case 0x81: case 0x82: case 0x83: case 0x84: case 0x85: case 0x86: case 0x87:
                case 0x88: case 0x89: case 0x8a: case 0x8b: case 0x8c: case 0x8d: case 0x8e: case 0x8f:
                case 0x90: case 0x91: case 0x92: case 0x93: case 0x94: case 0x95: case 0x96: case 0x97:
                case 0x98: case 0x99: case 0x9a: case 0x9b: case 0x9c: case 0x9d: case 0x9e: case 0x9f:
                case 0xa0: case 0xa1: case 0xa2: case 0xa3: case 0xa4: case 0xa5: case 0xa6: case 0xa7:
                case 0xa8: case 0xa9: case 0xaa: case 0xab: case 0xac: case 0xad: case 0xae: case 0xaf:
                case 0xb0: case 0xb1: case 0xb2: case 0xb3: case 0xb4: case 0xb5: case 0xb6: case 0xb7:
                case 0xb8: case 0xb9: case 0xba: case 0xbb: case 0xbc: case 0xbd: case 0xbe: case 0xbf:
                {
                    // one-octet compact int (-x10 to x3f, x90 is 0)
                    lResult = HessianInteger.valueOf(aLook - 0x90);
                    break;
                }
                case 0xc0: case 0xc1: case 0xc2: case 0xc3: case 0xc4: case 0xc5: case 0xc6: case 0xc7:
                case 0xc8: case 0xc9: case 0xca: case 0xcb: case 0xcc: case 0xcd: case 0xce: case 0xcf:
                {
                    // two-octet compact int (-x800 to x3ff)
                    readBuffer(1, "int (2-octet)");
                    lResult = HessianInteger.valueOf(((aLook - 0xc8) << 8 | (buffer[0] & 0xff)));
                    break;
                }
                case 0xd0: case 0xd1: case 0xd2: case 0xd3: case 0xd4: case 0xd5: case 0xd6: case 0xd7:
                {
                    // three-octet compact int (-x40000 to x3ffff).
                    readBuffer(2, "int (3-octet");
                    lResult = HessianInteger.valueOf(((aLook - 0xd4) << 16 | (buffer[0] & 0xff) << 8 | (buffer[1] & 0xff)));
                    break;
                }
                case 0xd8: case 0xd9: case 0xda: case 0xdb: case 0xdc: case 0xdd: case 0xde: case 0xdf:
                case 0xe0: case 0xe1: case 0xe2: case 0xe3: case 0xe4: case 0xe5: case 0xe6: case 0xe7:
                case 0xe8: case 0xe9: case 0xea: case 0xeb: case 0xec: case 0xed: case 0xee: case 0xef:
                {
                    // one-octet compact long (-x8 to x10, xe0 is 0).
                    lResult = HessianLong.valueOf((long) (aLook - 0xe0));
                    break;
                }
                case 0xf0: case 0xf1: case 0xf2: case 0xf3: case 0xf4: case 0xf5: case 0xf6: case 0xf7:
                case 0xf8: case 0xf9: case 0xfa: case 0xfb: case 0xfc: case 0xfd: case 0xfe: case 0xff:
                {
                    // two-octet compact long (-x800 to x3ff, xf8 is 0).
                    readBuffer(1, "long (2-octet)");
                    final long lLong = ((aLook - 0xf8) << 8 | (buffer[0] & 0xff));
                    lResult = HessianLong.valueOf(lLong);
                    break;
                }
                default:
                    // If we get here, then we missed a control character in the above switch.
                    // In this case we should correct the above switch.
                    throw new HessianParserException(String.format("Internal parser error, don't know control character %1$x on position: %2$d in the stream.", aLook, pos));
            } // switch
        } while (lResult instanceof HessianClassdef);

        return lResult;
    }

    private void reserved(int aCode)
    throws HessianParserException
    {
        throw new HessianParserException(String.format("Encountered a reserved byte 0x%1$x on position: %2$d in the stream.", aCode, (pos - 1)));
    }

    private void readBuffer(byte[] aBuffer, int aLen, String aWhat)
    throws HessianParserException
    {
        try
        {
            // We fill the buffer with data, starting from the beginnng of the buffer.
            int lBufPos = 0;
            while(aLen > 0)
            {
                final int lRead = stream.read(aBuffer, lBufPos, aLen);
                if (lRead < 0) throw new HessianParserException(String.format("Not enough bytes available while reading %1$s on position: %2$d. Requested: %3$d bytes and received: %4$d bytes.", aWhat, pos, aLen, lRead));
                aLen -= lRead;
                lBufPos += lRead;
            }
            pos += lBufPos;
        }
        catch (IOException e)
        {
            throw new HessianParserException(String.format("Unexpected I/O error while reading %1$s on position: %2$d.", aWhat, pos), e);
        }
    }

    private void readBuffer(int aLen, String aWhat)
    throws HessianParserException
    {
        // First we check to see if the current buffer is large enough to read the chunk.
        if (aLen > buffer.length) buffer = new byte[Math.round(1.2f * aLen)];
        readBuffer(buffer, aLen, aWhat);
    }

    private byte[] readAndAllocateBuffer(int aLen, String aWhat)
    throws HessianParserException
    {
        final byte[] lBuf = new byte[aLen];
        readBuffer(lBuf, aLen, aWhat);
        return lBuf;
    }

    private String readString(String aWhat)
    throws HessianParserException
    {
        final long lStartPos = pos;
        try
        {
            // First we read the length.
            readBuffer(2, aWhat);
            final int lLen = ((buffer[0] & 0xff) << 8 | (buffer[1] & 0xff));
            // Secondly we read the type string.
            readBuffer(lLen, aWhat);
            return new String(buffer, 0, lLen, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new HessianParserException(String.format("Corrupt string content while reading %1$s at position: %2$d.", aWhat, lStartPos), e);
        }
    }

    private void checkTypeRef(long aPos, int aRef)
    throws HessianParserException
    {
        if (aRef >= types.size())
            throw new HessianParserException(String.format("Reference to unexisting type at position: %1$d; reference: %2$d; available types: %3$d.", aPos, aRef, types.size()));
    }

    private void readAndCheckProtocolVersion(String aWhat)
    throws HessianParserException
    {
        final long lVersionStart = pos;
        readBuffer(2, "protocol version ('x02','x00')");
        final int aMajor = buffer[0] & 0xff;
        final int aMinor = buffer[1] & 0xff;
        if (aMajor != 0x02 || aMinor != 0x00)
            throw new HessianParserException(String.format("Expected 2.00-style %1$s but found other version: %2$d.%3$d at position: %4$d.", aWhat, aMajor, aMinor, lVersionStart));
    }
}