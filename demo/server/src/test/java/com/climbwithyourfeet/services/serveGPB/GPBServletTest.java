package com.climbwithyourfeet.services.serveGPB;

import com.climbwithyourfeet.proto.ExampleMessageProto;
import com.climbwithyourfeet.proto.ExampleMessageProto.ExampleMsg;
import java.util.List;
import java.io.IOException;
import org.junit.After;
import static org.junit.Assert.*;
import com.google.protobuf.GeneratedMessage.Builder;
import com.google.code.proto.streamio.*;
import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;

/**
 *
 * @author nichole
 */
public class GPBServletTest extends LocalServiceTestCase {

    protected Logger log = Logger.getLogger(this.getClass().getName());
    
    private boolean isOnline = false;

    public GPBServletTest() {
        super();
    }

    public void setUp() throws Exception {

        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetDefaultMessages() throws Exception {
        String path = "/gpb";
        
        assertMessages(path);
    }
    
    public void testGetDefaultMessagesWith1252Encoding() throws Exception {
        
        String ct = URLEncoder.encode("text/plain");
        
        String et = URLEncoder.encode("Windows-1252");
        
        String path = "/gpb?et=" + et + "&" + ct;
        
        assertMessages(path);
    }

    public void assertMessages(String pathAndQuery) throws Exception {
            
        String urlStr = "http://127.0.0.1:8080" + pathAndQuery;

        GPBServlet servlet = new GPBServlet();
        ServletConfig servletConfig = getServletConfig("GPBServlet");
        servlet.init(servletConfig);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
            
        servlet.doGet(request, response);

        byte[] contentBytes = response.getContent();

        String content = (contentBytes != null) ? new String(contentBytes, "UTF-8") : "";

        log.log(Level.INFO, "read serialized message: {0}", 
            new Object[]{content});
        
        int responseCode = response.getResponseCode();

        if (responseCode != 200) {
            fail(content);
        }

        assertTrue(responseCode == 200);

        Builder builder = ExampleMsg.newBuilder();

        ByteArrayInputStream in = null;

        try {
            in = new ByteArrayInputStream(contentBytes);
                    
            // reading the messages that were generated with Google's
            // built-in delimiters

            List<ExampleMessageProto.ExampleMsg> messages = new ArrayList<ExampleMsg>();

            ExampleMsg msg = ExampleMsg.parseDelimitedFrom(in);

            ExampleMsg msg2 = ExampleMsg.parseDelimitedFrom(in);

            log.log(Level.FINE, "read serialized message: {0}", new Object[]{msg.toString()});

            log.log(Level.FINE, "read serialized message: {0}", new Object[]{msg2.toString()});

            assertNotNull(msg);

            assertNotNull(msg2);

            assertTrue(!msg.toString().equals(msg2.toString()));

        } catch (Throwable t) {
            fail(t.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }
    }
    
    protected ServletConfig getServletConfig(String servletName) {

        Map<String, List<String>> initParameters = new HashMap<String, List<String>>();

        MockServletContext context = new MockServletContext(initParameters);
        
        MockServletConfig servletConfig = new MockServletConfig(context, servletName);

        return servletConfig;
    }

}
