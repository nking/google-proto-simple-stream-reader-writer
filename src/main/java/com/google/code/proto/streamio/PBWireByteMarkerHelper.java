package com.google.code.proto.streamio;

import com.google.protobuf.GeneratedMessage;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility class to help write the delimiting byte markers for Google protocol buffer 
 * generated messages used with PBStreamReader.
 *
 *  
 * @author nichole
 */
public class PBWireByteMarkerHelper {
    
    /**
     * Web-socket friendly start byte marker for delimiter
     */
    public static final byte markerForStart = (byte)0x80;
    
    // To follow up on complete compliance:
    //http://tools.ietf.org/html/draft-hixie-thewebsocketprotocol-75
    //  Frames denoted by bytes that have the high bit set (0x80 to
    //0xFF) have a leading length indicator, which is encoded as a series
    //of 7-bit bytes stored in octets with the 8th bit being set for all
    //but the last byte.  The remainder of the frame is then as much data
    //as was specified.
    
    /**
     * Size of the message size containing portion of the delimeter
     */
    public static final int byteMarkerSize = 4;
    
    /**
     * The total size of a delimeter (used when marshalling/unmarshalling GeneratedMessages)
     */
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
     * Create a delimiter for the given GeneratedMessage.
     * 
     * @param message instance of GeneratedMessage
     * @return byte array of size delimiterSize, to be used preceding serialized GeneratedMessages
     *   in a stream.  The delimiter to separate serialized objects is 
     *   a start byte of 0x80 followed by a 4-byte big-endian integer of the length of bytes.
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
     * Convert the integer into a byte array composed of byte shifted parts of the integer.
     * 
     * @param sz the integer to be represented by the returned byte array
     * @return 
     */
     static byte[] integerTo4ByteBigEndian(int sz) {
         
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
     * Convert the marker byte array to an integer where each item is part of byte shifted integer
     * 
     * @param marker
     * @return 
     */
     static int bytesToInteger(byte[] marker) {
        
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
