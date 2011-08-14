package com.google.code.proto.streamio;

import com.google.protobuf.GeneratedMessage;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to help write the start marker for Google protocol buffer items
 * that are to be serialized and written to a stream.
 * 
 * The delimiter to separate serialized objects is 
 *     a start byte of 0x80 followed by a 4-byte big-endian integer of the length of bytes
 * 
 * @author nichole
 */
public class PBWireByteMarkerHelper {
    
    public static final byte markerForStart = (byte)0x80;
    
    public static final int byteMarkerSize = 4;
    
    // 1 byte start + 4 bytes for length
    public static final int delimiterSize = byteMarkerSize + 1;

    public static int estimateTotalContentLength(List<? extends GeneratedMessage> messages) {

        int sum = 0;

        for (GeneratedMessage message : messages) {
            
            int messageSize =  message.getSerializedSize();
            
            Logger.getLogger(PBWireByteMarkerHelper.class.getName())
                .log(Level.INFO, "size of a message = {0}", Integer.toString(messageSize));

            sum += messageSize + delimiterSize ;
        }

        return sum;
    }
  
    /**
     * create a 5 byte marker for the start of the item to be serialized.
     * the first byte is markerForStart and the next 4 hold the size of the
     * subsequent serialized data.
     * 
     * @param message
     * @return a byte array of size 5 for start of serialized data
     */
    public static byte[] createMessageDelimiter(GeneratedMessage message) {
        
        Integer sz = message.getSerializedSize();
        
        byte[] szInBytes = integerTo4ByteBigEndian(sz);

        byte[] delimeter = new byte[PBWireByteMarkerHelper.delimiterSize];
        delimeter[0] = markerForStart;
        System.arraycopy(szInBytes, 0, delimeter, 1, 4);
        
        return delimeter;
    }

    /**
     * convert the integer into a byte array composed of byte shifted parts of the integer.
     * 
     * @param sz the integer to be represented by the returned byte array
     * @return 
     */
    public static byte[] integerTo4ByteBigEndian(int sz) {
         
        byte[] marker = new byte[4];
        
        int a00 = (sz) & 0xff;
        int a08 = (sz >>= 8) & 0xff;
        int a16 = (sz >>= 8) & 0xff;
        int a24 = (sz >>= 8) & 0xff;
        
        /*
         * int          byte
         * -----        -----
         * 0            0
         * 127          127
         * 128         -128
         * 255         -1
         */
        
        byte b00 = ((a00 >= 0) && (a00 < 128)) ? (byte)a00 : (byte)(a00 - 256);
        byte b08 = ((a08 >= 0) && (a08 < 128)) ? (byte)a08 : (byte)(a08 - 256);
        byte b16 = ((a16 >= 0) && (a16 < 128)) ? (byte)a16 : (byte)(a16 - 256);
        byte b24 = ((a24 >= 0) && (a24 < 128)) ? (byte)a24 : (byte)(a24 - 256);
        
        marker[0] = b00;
        marker[1] = b08;
        marker[2] = b16;
        marker[3] = b24;
        
        return marker;
    }
    
    /**
     * convert the marker byte array to an integer where each item is part of byte shifted integer
     * 
     * @param marker
     * @return 
     */
    public static int bytesToInteger(byte[] marker) {
        
        byte b00 = (byte)marker[0];
        byte b08 = (byte)marker[1];
        byte b16 = (byte)marker[2];
        byte b24 = (byte)marker[3];
        
        /*
         * int          byte
         * -----        -----
         * 0            0
         * 127          127
         * 128         -128
         * 255         -1
         */
        
        int d00 = ((b00 >= 0) && (b00 < 128)) ? (int)b00 : 256 + (int)b00;
        int d08 = ((b08 >= 0) && (b08 < 128)) ? (int)b08 : 256 + (int)b08;
        d08 = (d08 & 0xff) << 8;
        
        int d16 = ((b16 >= 0) && (b16 < 128)) ? (int)b16 : 256 + (int)b16;
        d16 = (d16 & 0xff) << 16;
        
        int d24 = ((b24 >= 0) && (b24 < 128)) ? (int)b24 : 256 + (int)b24;
        d24 = (d24 & 0xff) << 24;
        
        int total = d00 + d08 + d16 + d24;
        
        return total;
    }
   
}
