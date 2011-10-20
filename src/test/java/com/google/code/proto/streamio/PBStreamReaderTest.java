package com.google.code.proto.streamio;

import com.google.code.proto.model.ExampleMessageProto;
import com.google.code.proto.model.ExampleMessageProto.ExampleMsg;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;
import com.google.protobuf.AbstractMessage.Builder;

import junit.framework.TestCase;

/**
 *
 * @author nichole
 */
public class PBStreamReaderTest extends TestCase {

    private PipedOutputStream pipedOut = null;
    private PipedInputStream pipedIn = null;
    
    private final List<ExampleMessageProto.ExampleMsg> messages = new ArrayList<ExampleMsg>();
    
    private final List<ExampleMsg> results = new ArrayList<ExampleMsg>();
    
    public PBStreamReaderTest() {
        super();
    }

    public void setUp() throws Exception {
        super.setUp();
        
        pipedIn = new PipedInputStream(1024);
        pipedOut = new PipedOutputStream(pipedIn);
        
    }

    public void tearDown() throws Exception {
        super.tearDown();
        if (pipedOut != null) {
            pipedOut.close();
        }
        if (pipedIn != null) {
            pipedIn.close();
        }
    }
    
    @Test
    public void testReadUnsigned() throws Exception {

        // test whether the message is encoded using unsigned bytes (hopefully true)
        String name1 = "a title with more than 128 characters to see if the string length decoded length from the preceding varint is only wrong in the javascript parser protobuf.js";
        String value1 = "a value";
        int code1 = 200;
        
        String name2 = "a protocol message";
        String value2 = "";
        int code2 = 201;
        
        ExampleMsg.Builder msg = ExampleMsg.newBuilder();
        msg.setName(name1);
        msg.setValue(value1);
        msg.setCode(code1);
        
        messages.add(msg.build());
        
        msg.clear();
        msg.setName(name2);
        msg.setValue(value2);
        msg.setCode(code2);
        
        messages.add(msg.build());
        
        final CountDownLatch writeLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(1);

        Reader reader = new Reader(writeLatch, doneLatch);
          
        reader.start();
        
        PBStreamWriter.writeToStream(pipedOut, messages); 
        pipedOut.close();
        pipedOut = null;
        writeLatch.countDown();
        
        doneLatch.await();
        
        assertTrue(results.size() == messages.size());
        
        ExampleMsg r1 = results.get(0);
        ExampleMsg r2 = results.get(1);
        
        assertEquals(r1.getName(), name1);
        assertEquals(r1.getValue(), value1);
        assertTrue(r1.getCode() == code1);
        
        assertEquals(r2.getName(), name2);
        assertEquals(r2.getValue(), value2);
        assertTrue(r2.getCode() == code2);
    }
    
    private class Reader extends Thread {
        private CountDownLatch writeLatch = null;
        private CountDownLatch doneLatch = null;
        Reader(CountDownLatch writeLatch, CountDownLatch doneLatch) {
            this.writeLatch = writeLatch;
            this.doneLatch = doneLatch;
        }
        @Override
        public void run() {
            try {
                writeLatch.await();
                PBStreamReader<ExampleMsg> pbReader = new PBStreamReader();
                Builder builder = ExampleMsg.newBuilder();
                List<ExampleMsg> res = pbReader.read(pipedIn, builder);
                results.addAll(res);
                doneLatch.countDown();
            } catch (Exception e) {
                fail(e.getMessage());
                doneLatch.countDown();
            }
        }
    }
    
}
