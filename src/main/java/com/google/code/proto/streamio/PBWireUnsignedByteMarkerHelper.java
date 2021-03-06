package com.google.code.proto.streamio;

/**
 * A utility class to help write delimiting byte markers for Google protocol buffer
 * generated messages used with PBStreamReader.  The delimeter is a byte array holding
 * the size of the generated message that will follow.
 *
 * The delimiter is a unsigned byte marker that can be used by clients that can
 * only process ASCII strings.
 *
 * @author nichole
 */
public class PBWireUnsignedByteMarkerHelper extends AbstractPBWireByteMarkerHelper {

     /**
     * Convert the integer into a byte array composed of byte shifted parts of the integer.
     * The byte array has been restricted to the signed portion to accommodate clients
     * that can only process strings
     * (ascii being 0-127.  extended ascii is 128-255 but not all clients can read that).
     *
     * @param value the integer to be represented by the returned byte array
     * @return big endian byte array holding the value
     */
    @Override
    public byte[] integerToBytesBigEndian(int value) {

        byte[] marker = new byte[byteMarkerSize];

        for (int i = 0; i < marker.length; i++) {
            int shift = i * 7;
            long a = (value >> shift) & 0x7f;
            byte b = (byte)a;
            marker[byteMarkerSize - i - 1] = b;
        }
       
        return marker;
    }

     /**
     * Convert the marker byte array to an integer where each item is part of byte shifted integer
     * The byte array has been restricted to the signed portion to accommodate clients
     * that can only process strings (ascii being 0-127).  The maximum value that can be
     * returned is 2^(8*byteMarkerSize - 1) which is 2GB for a byteMarkerSize = 4.
     *
     * @param marker big endian byte array holding the value
     * @return original value held in the marker
     */
    @Override
     public int bytesToInteger(byte[] marker) {

        /*
         *  byte     int
         *  ----     -----
         *  0        0
         *  127      127
         */
        int total = 0;
        for (int i = 0; i < marker.length; i++) {
            int shift = (marker.length - i - 1) * 7;
            byte b = marker[i];
            if (b > 127) {
                throw new IllegalArgumentException("byte marker contains a value less than 0 which is not allowed");
            }
            int d = b;
            d = d << shift;
            total += d;
        }
        
        if (total > Math.pow(2, (8 * byteMarkerSize) - 1)) {
            throw new IllegalArgumentException("the value sz exceeds the max holdable in these byte markers");
        }
        
        return total;
    }
}
