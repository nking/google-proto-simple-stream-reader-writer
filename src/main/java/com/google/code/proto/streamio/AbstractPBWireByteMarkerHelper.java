/*
 * The MIT License
 *
 * Copyright 2011 Climb With Your Feet.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.google.code.proto.streamio;

import com.google.protobuf.GeneratedMessage;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class to help write delimiting byte markers for Google protocol buffer 
 * generated messages used with PBStreamReader.  The delimeter is a byte array holding
 * the size of the generated message that will follow.
 * 
 * @author nichole
 */
public abstract class AbstractPBWireByteMarkerHelper implements IPBWireByteMarkerHelper {

    protected final byte markerForStart = (byte) 0;
     
    /**
     * Number of bytes holding a message size, where this number of bytes follows the
     * byteMarker within a delimiter.  The largest number it can hold is roughly 2G.
     */
    protected int byteMarkerSize = 4;
    
    /**
     * The total number of bytes of a delimeter for a generated message.
     */
    protected int delimiterSize = byteMarkerSize + 1;
   
    /**
     * Convert the marker byte array to an integer
     *
     * @param marker
     * @return
     */
    @Override
    public abstract int bytesToInteger(byte[] marker);

    /**
     * Convert the integer into a signed byte array composed of byte shifted parts of the integer.
     *
     * @param sz the integer to be represented by the returned byte array
     * @return
     */
    @Override
     public abstract byte[] integerToBytesBigEndian(int sz);
     
    /**
     * Create a delimiter for the given GeneratedMessage.
     *
     * @param message instance of GeneratedMessage
     * @return byte array of size delimiterSize, to be used preceding serialized GeneratedMessages
     *   in a stream.  The delimiter to separate serialized objects is
     *   a start byte of 0x80 followed by a 4-byte big-endian integer of the length of bytes.
     */
    @Override
    public byte[] createMessageDelimiter(GeneratedMessage message) {
        
        Integer sz = message.getSerializedSize();
        
        byte[] szInBytes = integerToBytesBigEndian(sz);
        
        byte[] delimeter = new byte[delimiterSize];
        
        delimeter[0] = markerForStart;
        
        System.arraycopy(szInBytes, 0, delimeter, 1, byteMarkerSize);
        
        return delimeter;
    }

    /**
     * Estimate the total number of bytes that will be used in a stream for a list
     * of messages - this includes delimeters.  This is useful to set the content-length in an HTTP response.
     *
     * @param messages instances of GeneratedMessage
     * @return total number of bytes needed for a list of generated messages.
     * The total includes the delimiters too.
     */
    @Override
    public int estimateTotalContentLength(List<? extends GeneratedMessage> messages) {
        
        int sum = 0;
        
        for (GeneratedMessage message : messages) {
        
            int messageSize = message.getSerializedSize();
            
            Logger.getLogger(PBWireByteMarkerHelper.class.getName()).log(Level.FINEST, "size of a message = {0}", Integer.toString(messageSize));
            
            sum += messageSize + delimiterSize;
        }
        
        return sum;
    }

    /**
     * Get the number of bytes holding a message size, where these bytes are within
     * the delimiter byte array and follow the byte marker.
     *
     * @return number of bytes holding a message size.
     */
    @Override
    public int getByteMarkerSize() {
        return byteMarkerSize;
    }

    /**
     * Get the total number of bytes of a delimeter for a generated message.
     *
     * @return the number of bytes in a delimiter
     */
    @Override
    public int getDelimiterSize() {
        return delimiterSize;
    }

    /**
     * Get the byte marker used to identify the start of the delimiter.
     *
     * @return byte marker used to identify the start of the delimiter
     */
    @Override
    public byte getMarkerForStart() {
        return markerForStart;
    }
    
}
