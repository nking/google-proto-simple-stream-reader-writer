package com.google.code.proto.streamio;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.GeneratedMessage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A writer for an output stream of Google protocol buffer generated messages.
 * 
 * @author nichole
 */
public class PBStreamWriter {

    /**
     * Write GeneratedMessages to the given output stream by writing a 
     * websocket style byte marker that includes
     * the proceeding message length and write the next encoded serialized message, 
     * repeating for list of events
     * 
     * @param out
     * @param messages messages generated by Google's protocol buffer
     * @throws IOException 
     */
    public static void writeToStream(OutputStream out, List<? extends GeneratedMessage> messages) throws IOException {

        for (GeneratedMessage message : messages) {
            // delimiter will follow Websockets:
            // 0x80 followed by length in a 4-byte big-endian integer
            byte[] delimiter = PBWireByteMarkerHelper.createMessageDelimiter(message);

            out.write(delimiter);

            CodedOutputStream cos = CodedOutputStream.newInstance(out);
            message.writeTo(cos);
            cos.flush();
            out.flush();
            
            Logger.getLogger(PBStreamWriter.class.getName()).log(
                Level.INFO, "===> Wrote {0} bytes to stream + {1} bytes for delimeter",
                new Object[]{
                    Integer.toString(message.getSerializedSize()),
                    Integer.toString(delimiter.length)
                });

        }
    }
   
}
