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
     * @param value the integer to be represented by the returned byte array
     * @return value in signed integer big endian bytes format
     */
    @Override
    public byte[] integerToBytesBigEndian(int value) {

        byte[] marker = new byte[byteMarkerSize];
        
        for (int i = 0; i < marker.length; i++) {
            int shift = i * 8;
            int a = (value >> shift) & 0xff;
            byte b = (byte)a;
            marker[byteMarkerSize - i - 1] = b;
        }
      
        return marker;
    }
     
     /**
     * Convert the marker byte array to an integer where each item in marker array is part of byte shifted integer
     * 
     * @param marker a byte array in big endian bytes format
     * @return the original value of the byte array as a signed integer
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
            int shift = (marker.length - i - 1) * 8;
            byte b = marker[i];
            int d = (b > -1) ? b : 256 + b;
            d = d << shift;
            total += d;
        }
       
        return total;
    }
}
