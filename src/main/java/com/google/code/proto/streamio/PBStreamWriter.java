package com.google.code.proto.streamio;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.GeneratedMessage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A writer for an output stream of Google protocol buffer generated messages.
 * <br />
 * <br />
 * <b>Usage:</b><br />
 * {@code List<YourProtocolBufferMessage> messages = getGPBMessages...}
 * <br />
 * {@code PBStreamWriter.writeToStream(out, messages); }
 * 
 * @author nichole
 */
public class PBStreamWriter {

    private static Logger log = Logger.getLogger(PBStreamWriter.class.getName());
    
    /**
     * Write GeneratedMessages to the given output stream.
     * 
     * It uses a byte marker and array for delimiting messages.  
     * Note the default here is to use the bytemarker for signed bytes (only uses range 0-127).
     * 
     * @param out output stream being written to
     * @param messages messages generated by Google's protocol buffer
     * @throws IOException 
     */
    public static void writeToStream(OutputStream out, List<? extends GeneratedMessage> messages) throws IOException {

         writeToStream(out, messages, new PBWireSignedByteMarkerHelper());
    }
    
    /**
     * Write GeneratedMessages to the given output stream, 
     * and use the a specialized PBWireByteMarkerHelper to write the delimiters.
     * 
     * @param out output stream being written to
     * @param messages messages generated by Google's protocol buffer
     * @param gpbWireByteMarkerHelper instance of IPBWireByteMarkerHelper used to write delimiters
     * @throws IOException 
     */
    public static void writeToStream(OutputStream out, List<? extends GeneratedMessage> messages,
        IPBWireByteMarkerHelper gpbWireByteMarkerHelper) throws IOException {

        int count = 1;
        for (GeneratedMessage message : messages) {
            
            byte[] delimiter = gpbWireByteMarkerHelper.createMessageDelimiter(message);

            log.log(Level.FINEST, "Writing byte marker for message {0}, size of message is {1}", 
                new Object[]{Integer.valueOf(count), 
                    Integer.valueOf(gpbWireByteMarkerHelper.bytesToInteger(Arrays.copyOfRange(delimiter, 1, delimiter.length)))});
            
            /*for (int i = 1; i < delimiter.length; i++) {
                log.log(Level.INFO, "bytemarker[{0}]={1}", 
                new Object[]{Integer.valueOf(i), Byte.toString(delimiter[i])} );
            }*/
            
            out.write(delimiter);

            CodedOutputStream cos = CodedOutputStream.newInstance(out);
            message.writeTo(cos);
            cos.flush();
            out.flush();

            count++;
        }
    }
   
}
