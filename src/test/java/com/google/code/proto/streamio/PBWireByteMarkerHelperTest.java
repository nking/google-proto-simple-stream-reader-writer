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
    public void testIntegerToSignedByteBigEndian() {
        
        PBWireSignedByteMarkerHelper pbh = new PBWireSignedByteMarkerHelper();
        
        int sz = 100000;
        byte[] marker = pbh.integerToBytesBigEndian(sz);
        
        assertNotNull(marker);
        
        int sz2 = pbh.bytesToInteger(marker);
        
        assertTrue(sz == sz2);
    }

    
    @Test
    public void testUnsignedBytesToInteger() {
        // initialized but empty byte marker returns zero
        PBWireUnsignedByteMarkerHelper pbh = new PBWireUnsignedByteMarkerHelper();
        
        byte[] marker = new byte[pbh.getDelimiterSize() - 1];
        
        int sz = pbh.bytesToInteger(marker);
        
        assertTrue(sz == 0);
    }
    
    @Test
    public void testCreateMessageSignedByteMarker() {
        
        String name1 = "message name";
        String value1 = "a value";
        int code1 = 200;
        
        ExampleMsg.Builder msg = ExampleMsg.newBuilder();
        msg.setName(name1);
        msg.setValue(value1);
        msg.setCode(code1);
        ExampleMsg message = msg.build();
        
        int sz = message.getSerializedSize();
        
        PBWireSignedByteMarkerHelper pbh = new PBWireSignedByteMarkerHelper();
        
        byte[] delimiter = pbh.createMessageDelimiter(message);
        
        assertNotNull(delimiter);
        assertTrue(delimiter[0] == pbh.getMarkerForStart());
        
        byte[] lengthByteMarker = Arrays.copyOfRange(delimiter, 1, delimiter.length);
        
        int sz2 = pbh.bytesToInteger(lengthByteMarker);
        
        assertTrue(sz == sz2);
    }
    
    /**
     * Test of calculateEventByteMarker method, of class PBWireHelper.
     */
    @Test
    public void testIntegerToUnsignedByteBigEndian() {
        
        PBWireUnsignedByteMarkerHelper pbh = new PBWireUnsignedByteMarkerHelper();
        
        int sz = 100000;
        byte[] marker = pbh.integerToBytesBigEndian(sz);
        
        assertNotNull(marker);
        
        int sz2 = pbh.bytesToInteger(marker);
        
        assertTrue(sz == sz2);
    }

    
    @Test
    public void testSignedBytesToInteger() {
        // initialized but empty byte marker returns zero
        PBWireSignedByteMarkerHelper pbh = new PBWireSignedByteMarkerHelper();
        
        byte[] marker = new byte[pbh.getDelimiterSize() - 1];
        
        int sz = pbh.bytesToInteger(marker);
        
        assertTrue(sz == 0);
    }
    
    @Test
    public void testCreateMessageUnsignedByteMarker() {
        
        String name1 = "message name";
        String value1 = "a value";
        int code1 = 200;
        
        ExampleMsg.Builder msg = ExampleMsg.newBuilder();
        msg.setName(name1);
        msg.setValue(value1);
        msg.setCode(code1);
        ExampleMsg message = msg.build();
        
        int sz = message.getSerializedSize();
        
        PBWireUnsignedByteMarkerHelper pbh = new PBWireUnsignedByteMarkerHelper();
        
        byte[] delimiter = pbh.createMessageDelimiter(message);
        
        assertNotNull(delimiter);
        assertTrue(delimiter[0] == pbh.getMarkerForStart());
        
        byte[] lengthByteMarker = Arrays.copyOfRange(delimiter, 1, delimiter.length);
        
        int sz2 = pbh.bytesToInteger(lengthByteMarker);
        
        assertTrue(sz == sz2);
    }
    
    @Test
    public void testTmp() {
        
        PBWireUnsignedByteMarkerHelper pbh = new PBWireUnsignedByteMarkerHelper();

        int sz = 275;
        byte[] byteMarker = pbh.integerToBytesBigEndian(sz);
        

        int rsz = pbh.bytesToInteger(byteMarker);
        
        PBWireSignedByteMarkerHelper pbh2 = new PBWireSignedByteMarkerHelper();
        byte[] byteMarker2 = new byte[]{19,1,0,0};
        int sz2 = pbh2.bytesToInteger(byteMarker2);
        
        int i = 1;
    }
}
