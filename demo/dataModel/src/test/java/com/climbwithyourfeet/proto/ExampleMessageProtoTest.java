package com.climbwithyourfeet.proto;

import com.climbwithyourfeet.proto.ExampleMessageProto.ExampleMsg;
import java.util.logging.Logger;
import org.junit.After;
import static org.junit.Assert.*;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 * @author nichole
 */
public class ExampleMessageProtoTest extends TestCase {
 
    private final List<ExampleMessageProto.ExampleMsg> messages = new ArrayList<ExampleMsg>();
    
    private transient Logger log = Logger.getLogger(this.getClass().getName());
    
    String name1 = "a title with more than 128 characters to see if the string length decoded length from the preceding varint is only wrong in the javascript parser protobuf.js";
    String value1 = "a value";
    int code1 = 200;
    String name2 = "a protocol message";
    String value2 = "";
    int code2 = 201;

    public ExampleMessageProtoTest() {
        
        super();
    }

    public void setUp() throws Exception {
        super.setUp();
         
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
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testWriteTo() throws Exception {
        
        ExampleMsg msg1 = messages.get(0);
                
        int bufferSize = 2048;
        
        byte[] wroteBytes = new byte[bufferSize];
        CodedOutputStream cos = CodedOutputStream.newInstance(wroteBytes);

        msg1.writeTo(cos);
        
        int spaceLeft = cos.spaceLeft();
        int nWrote = bufferSize - spaceLeft;
        
        assertNotNull(wroteBytes);

        assertTrue(nWrote > 0);
        
    }
    
    public void testParseFrom() throws Exception {
        
        ExampleMsg msg1 = messages.get(0);
        
        int bufferSize = 2048;
        
        byte[] wroteBytes = new byte[bufferSize];
        CodedOutputStream cos = CodedOutputStream.newInstance(wroteBytes);

        msg1.writeTo(cos);
        
        int spaceLeft = cos.spaceLeft();
        int nWrote = bufferSize - spaceLeft;
        
        byte[] readTheseEncodedBytes = new byte[nWrote];
        
        System.arraycopy(wroteBytes, 0, readTheseEncodedBytes, 0, nWrote);
        
        CodedInputStream cis = CodedInputStream.newInstance(readTheseEncodedBytes);
        ExampleMsg result = ExampleMsg.parseFrom(cis);
        
        assertResults(result, messages.get(0));        
    }
    
    private void assertResults(ExampleMsg result, ExampleMsg msg) {
                
        assertNotNull(result);
        
        assertEquals(result.getName(), msg.getName());
        assertTrue(result.getCode() == msg.getCode());
        
        assertEquals(result.getValue(), msg.getValue());
    }

}
