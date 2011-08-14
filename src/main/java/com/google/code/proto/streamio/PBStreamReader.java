package com.google.code.proto.streamio;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.GeneratedMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A reader for an input stream of Google protocol buffer generated messages.
 * 
 * 
 * @author nichole
 */
public class PBStreamReader<T extends GeneratedMessage> {
    
    protected boolean finishedReadingStream = false;
    
    protected Logger log = Logger.getLogger(this.getClass().getName());

    public <T> PBStreamReader() {
        finishedReadingStream = false;
    }
       
    /**
     * Read until find the next startMarker and return the subsequent byteMarkerSize 
     * bytes in byteMarker.  
     * The return from the method itself is a byte array of the 
     * remaining bytes that were read from the stream after the byteMarker.
     * 
     * Note that if the stream has less than byteMarkerSize bytes in it, byteMarker will not have
     * been written to completely and an IOException will be thrown.
     * 
     * @param inStream
     * @param remnant
     * @param bufferSize
     * @param startByte
     * @param byteMarker 
     * @param byteMarkerSize this is the byteMarker minus the start byte size
     * @return byte array of bytes read from stream beyond the found byteMarker
     * @throws IOException
     */
    protected byte[] readUntilNextStartMarker(InputStream inStream, byte[] remnant, int bufferSize,
        byte startByte, byte[] byteMarker, int byteMarkerSize) throws IOException {
        
        log.log(Level.INFO, "readNextStartMarker");
        
        if ((byteMarker == null) || (byteMarker.length != byteMarkerSize)) {
            log.log(Level.SEVERE, "size of byteMarker must be equal to byteMarkerSize");
            throw new IllegalArgumentException("size of byteMarker must be equal to byteMarkerSize");
        }
        
        // We make a buffer of bufferSize to search for the bytemarker within.
        // If a remnant is passed in, store that first.
       
        int nRead = 0;

        int sum = 0;
        
        byte[] buffer;
        
        // ---   store all of remnant into buffer  ---
        if ((remnant != null) && (remnant.length > 0)) {
            sum = remnant.length;
            bufferSize = bufferSize + remnant.length;
            buffer = new byte[bufferSize];
            System.arraycopy(remnant, 0, buffer, 0, sum);
            remnant = null;
        } else {
            log.log(Level.FINE, "reading stream ");
            buffer = new byte[bufferSize];
        }
        while ((sum < bufferSize) && (nRead != -1)) {
            nRead = inStream.read(buffer, sum, (bufferSize - sum));
            if (nRead != -1)
                sum += nRead;
        }  
        if (nRead == -1) {
            finishedReadingStream = true;
            log.log(Level.INFO, "end of stream");
        } else {
            log.log(Level.FINE, "read {0} bytes from remnant and input stream", sum);           
        }
        if ((sum > 0) && (sum < byteMarkerSize)) {
            throw new IOException("stream has ended and we only read " + sum 
                + " bytes but needed to find " + byteMarkerSize + " bytes for the byte marker");
        }
        
        
        // ---------  find  bytemarker in buffer ---------
        // this is the position of the start byte which precedes the byte marker
        int matchStartPos = -1;
        int markerBytesFound = 0;
        
        int bufferTrimPos = 0;

        boolean foundMarker = false;
        boolean foundStartByte = false;
        
        searchForStart:
        while ((sum > 0) && !foundMarker) {
            for (int i = 0; i < sum; i++) {
                if (!foundStartByte && (buffer[i] == startByte)) {
                    foundStartByte = true;
                    matchStartPos = i;
                } else if (foundStartByte) {
                    markerBytesFound++;
                }
                
                if (foundStartByte && (markerBytesFound == byteMarkerSize)) {
                    // we found a match to startByte and have the byteMarker
                    foundMarker = true;
                    foundStartByte = false;
                    bufferTrimPos = i + 1;
                    // store the byte marker
                    System.arraycopy(buffer, (matchStartPos + 1), byteMarker, 0, byteMarkerSize);  // (Object src, int srcPos, Object dest, int destPos, int length)
                    break searchForStart;
                }
            }
            
            /* this shouldn't be reached.
             * the protocol is:
             *     bytemarker(containing message1 length) then message1 then bytemarker(containing message12 length) then message2  ...
             * so the method should always be called with the start marker bytes leading all subsequent bytes
             */
            throw new IOException("leading byte marker was not found in stream after reading " + bufferSize + " bytes");
        }

        if (sum > 0) {
            int start = (bufferTrimPos < sum) ? bufferTrimPos : sum;
            // -- store trailing bytes in remnant
            log.log(Level.INFO, "writing to remnant");
            if (remnant != null) {
                byte[] remn = new byte[remnant.length + sum];
                System.arraycopy(remnant, 0, remn, 0, remnant.length);  // (Object src, int srcPos, Object dest, int destPos, int length)
                System.arraycopy(remnant, remnant.length, buffer, start, sum - start);
                remnant = remn;
            } else {
                remnant = Arrays.copyOfRange(buffer, start, sum);
            }
        }
        return remnant;
    }
    
    /**
     * Read instances of GeneratedMessage from the input stream and use the 
     * given builder to unmarshall the messages.
     * 
     * @param inStream
     * @param messageBuilder
     * @return list of GeneratedMessage instances decoded and deserialized from input stream
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public List<T> read(InputStream inStream, final Builder messageBuilder) 
    throws IOException, InstantiationException, IllegalAccessException {
        
        log.log(Level.INFO, "read");
        
        List<T> results = new ArrayList<T>();
        
        int bufferSizeForMarkerReads = 256;

        byte[] remnant = null;
        
        while (!finishedReadingStream || ((remnant != null) && (remnant.length > 0))) {

            byte[] byteMarker = new byte[PBWireByteMarkerHelper.byteMarkerSize];

            remnant = readUntilNextStartMarker(inStream, remnant, bufferSizeForMarkerReads, 
                PBWireByteMarkerHelper.markerForStart, byteMarker, byteMarker.length);

            int messageLength = PBWireByteMarkerHelper.bytesToInteger(byteMarker);

            log.log(Level.INFO, "reading an event of length = {0}", messageLength);
            
            if (messageLength == 0) {
                finishedReadingStream = true;
                continue;
            }
            
            // ---- we have the byte marker, so append more of the stream to remnant bytes until we have the message amount
            while ( remnant.length < messageLength) {
                byte[] bytes = new byte[bufferSizeForMarkerReads];
                int nRead = inStream.read(bytes);
                if (nRead == -1) {
                    finishedReadingStream = true;
                    continue;
                }
                int sz = remnant.length + nRead;
                byte[] remn = new byte[sz];
                System.arraycopy(remnant, 0, remn, 0, remnant.length);
                System.arraycopy(bytes, 0, remn, remnant.length, nRead);
                remnant = remn;
            }
            
            // ----- we have message length bytes in remnant now and can decode and deserialize message -------
                
            byte[] messageBytes = new byte[messageLength];
            System.arraycopy(remnant, 0, messageBytes, 0, messageLength);

            CodedInputStream codedInStream = CodedInputStream.newInstance(messageBytes);
            messageBuilder.mergeFrom(codedInStream);
            
            T msg = (T) messageBuilder.build();
            log.log(Level.FINE, "read serialized message: {0}", new Object[]{msg.toString()});
            results.add( msg );
            
            messageBuilder.clear();
            
            if (remnant.length == messageLength) {
                remnant = null;
            } else {
                byte[] remn = Arrays.copyOfRange(remnant, messageLength, remnant.length);
                remnant = remn;
            }
        }

        log.log(Level.INFO, "read {0} results", new Object[]{ Integer.toString(results.size())});
        
        return results;
    }
 
}
