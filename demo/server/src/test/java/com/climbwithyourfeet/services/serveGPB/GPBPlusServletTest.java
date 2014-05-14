package com.climbwithyourfeet.services.serveGPB;

import com.climbwithyourfeet.proto.ExampleMessageProto.ExampleMsg;
import com.climbwithyourfeet.proto.ExampleMessageProto.ExampleMsg.Builder;
import com.google.code.proto.streamio.PBStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;

/**
 *
 * @author nichole
 */
public class GPBPlusServletTest extends LocalServiceTestCase {

    protected Logger log = Logger.getLogger(this.getClass().getName());
    
    private boolean isOnline = false;

    public GPBPlusServletTest() {
        super();
    }

    public void setUp() throws Exception {

        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetCustomDelimiterMessages() throws Exception {
        
        String path = "/gpbplus";
        
        assertMessages(path);
    }
    
    public void estMessagesWith1252Encoding() throws Exception {
        
        String ct = URLEncoder.encode("text/plain");
        
        String et = URLEncoder.encode("Windows-1252");
        
        String path = "/gpbplus?et=" + et + "&" + ct;
        
        assertMessages(path);
    }

    public void assertMessages(String pathAndQuery) throws Exception {
            
        String urlStr = "http://127.0.0.1:8080" + pathAndQuery;

        GPBPlusServlet servlet = new GPBPlusServlet();
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

            // reading messages that were generated with custom delimiters

            PBStreamReader streamReader = new PBStreamReader();

            List<ExampleMsg> messages = streamReader.read(in, builder);
            assertNotNull(messages);
            assertTrue(messages.size() == 2);

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
