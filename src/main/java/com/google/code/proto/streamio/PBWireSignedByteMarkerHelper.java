package com.google.code.proto.streamio;

/**
 * A utility class to help write delimiting byte markers for Google protocol buffer 
 * generated messages used with PBStreamReader.  The delimeter is a byte array holding
 * the size of the generated message that will follow.
 * 
 * The delimiter is an signed byte marker (uses full range -128 to +127, so don't read
 * these with a client that can only read characters 0-127).
 *
 * @author nichole
 */
public class PBWireSignedByteMarkerHelper extends AbstractPBWireByteMarkerHelper {
   
     /**
     * Convert the integer into a byte array composed of byte shifted parts of the integer.
     * 
     * 
     * @param sz the integer to be represented by the returned byte array
     * @return 
     */
    @Override
    public byte[] integerToBytesBigEndian(int sz) {

        byte[] marker = new byte[byteMarkerSize];
        
        for (int i = 0; i < marker.length; i++) {
            int shift = i * 8;
            int a = (sz >> shift) & 255;
            byte b = ((a >= 0) && (a < 128)) ? (byte) a : (byte)(a - 256);
            marker[i] = b;
        }
        
        return marker;
    }
     
     /**
     * Convert the marker byte array to an integer where each item in marker array is part of byte shifted integer
     * 
     * @param marker
     * @return 
     */
    @Override
     public int bytesToInteger(byte[] marker) {
         
        /*
         *  byte          int
         *  -----        -----
         *  0            0
         *  127          127
         * -128          128
         * -1            255
         */
         
        int total = 0;
        
        for (int i = 0; i < marker.length; i++) {
            byte b = marker[i];
            int d = ((b >= 0) && (b < 128)) ? (int)b : 256 + (int)b;
            int shift = i * 8;
            d = (d & 0xff) << shift;
            total += d;
        }
        
        return total;
    }
}
