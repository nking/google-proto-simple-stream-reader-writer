package com.google.code.proto.streamio;

import com.google.code.proto.model.ExampleMessageProto.ExampleMsg;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import junit.framework.TestCase;

/**
 *
 * @author nichole
 */
public class PBWireByteMarkerHelperTest extends TestCase {

    private Logger log = Logger.getLogger(this.getClass().getName());
    
    public PBWireByteMarkerHelperTest() {
        super();
    }

    /**
     * Test of calculateEventByteMarker method, of class PBWireHelper.
     */
    public void testIntegerToSignedByteBigEndian() throws Exception {

        PBWireSignedByteMarkerHelper pbh = new PBWireSignedByteMarkerHelper();

        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");

        long seed = System.currentTimeMillis();

        sr.setSeed(seed);
        log.info("SEED=" + seed);
        
        for (int i = 0; i < 100; ++i) {
            
            int v = sr.nextInt(1000000);
            
            byte[] bytes = pbh.integerToBytesBigEndian(v);
            
            BigInteger b = new BigInteger(bytes);
            
            long r = b.longValueExact();
            
            assertTrue(r == v);
        }
    }

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
    public void testIntegerToUnsignedByteBigEndian() throws Exception {

        PBWireUnsignedByteMarkerHelper pbh = new PBWireUnsignedByteMarkerHelper();

        int sz = 100000;
        byte[] marker = pbh.integerToBytesBigEndian(sz);

        assertNotNull(marker);

        int sz2 = pbh.bytesToInteger(marker);

        assertTrue(sz == sz2);
    }

    public void testSignedBytesToInteger() {
        // initialized but empty byte marker returns zero
        PBWireSignedByteMarkerHelper pbh = new PBWireSignedByteMarkerHelper();

        byte[] marker = new byte[pbh.getDelimiterSize() - 1];

        int sz = pbh.bytesToInteger(marker);

        assertTrue(sz == 0);
    }

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

    public void testTmp() {

        PBWireSignedByteMarkerHelper pbh = new PBWireSignedByteMarkerHelper();

        int sz = 511;
        byte[] byteMarker = pbh.integerToBytesBigEndian(sz);

        int rsz = pbh.bytesToInteger(byteMarker);
        
        assertTrue(sz == rsz);

        PBWireSignedByteMarkerHelper pbh2 = new PBWireSignedByteMarkerHelper();
        byte[] byteMarker2 = new byte[]{0, 0, 1, -1};
        int sz2 = pbh2.bytesToInteger(byteMarker2);

        assertTrue(sz2 == sz);
        
        /*
        v=127       bytes=[0, 0, 0, 127]
        v=511       bytes=[0, 0, 1, -1]
        v=512       bytes=[0, 0, 2, 0]
        v=33554432  bytes=[2, 0, 0, 0]        
        */
    }
}
