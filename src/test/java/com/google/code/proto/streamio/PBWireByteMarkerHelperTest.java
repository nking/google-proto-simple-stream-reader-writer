package com.google.code.proto.streamio;

import com.google.code.proto.model.ExampleMessageProto.ExampleMsg;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import junit.framework.TestCase;

/**
 *
 * @author nichole
 */
public class PBWireByteMarkerHelperTest extends TestCase {

    public PBWireByteMarkerHelperTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of calculateEventByteMarker method, of class PBWireHelper.
     */
    @Test
    public void testIntegerTo4ByteBigEndian() {
        
        int sz = 100000;
        byte[] marker = PBWireByteMarkerHelper.integerTo4ByteBigEndian(sz);
        
        assertNotNull(marker);
        
        int sz2 = PBWireByteMarkerHelper.bytesToInteger(marker);
        
        assertTrue(sz == sz2);
    }

    
    @Test
    public void testBytesToInteger() {
        // initialized but empty byte marker returns zero
        
        byte[] marker = new byte[PBWireByteMarkerHelper.delimiterSize - 1];
        
        int sz = PBWireByteMarkerHelper.bytesToInteger(marker);
        
        assertTrue(sz == 0);
    }
    
    /**
     */
    @Test
    public void testCreateMessageByteMarker() {
        
        String name1 = "message name";
        String value1 = "a value";
        int code1 = 200;
        
        ExampleMsg.Builder msg = ExampleMsg.newBuilder();
        msg.setName(name1);
        msg.setValue(value1);
        msg.setCode(code1);
        ExampleMsg message = msg.build();
        
        int sz = message.getSerializedSize();
        
        byte[] delimiter = PBWireByteMarkerHelper.createMessageDelimiter(message);
        
        assertNotNull(delimiter);
        assertTrue(delimiter[0] == (byte)0x80);
        
        byte[] lengthByteMarker = Arrays.copyOfRange(delimiter, 1, delimiter.length);
        
        int sz2 = PBWireByteMarkerHelper.bytesToInteger(lengthByteMarker);
        
        assertTrue(sz == sz2);
    }

}
