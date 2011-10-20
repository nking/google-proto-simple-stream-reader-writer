package com.climbwithyourfeet.services.serveGPB;

import com.climbwithyourfeet.proto.ExampleMessageProto.ExampleMsg;
import java.util.List;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedInputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import com.google.protobuf.GeneratedMessage.Builder;
import com.google.code.proto.streamio.*;
import junit.framework.TestCase;

/**
 *
 * @author nichole
 */
public class GPBServletTest extends TestCase {

    private boolean isOnline = false;
    
    public GPBServletTest() {
        super();
    }

    public void setUp() throws Exception {

        super.setUp();

        try {

            isOnline = isOnline();

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testGetMessages() throws Exception {
        
        // there's a prepped database in the test resources directory for sanfrancisco that should be used
        
        if (false & isOnline) {
            
            HttpRequestSender sender = null;
            
            String path = "/gpb";

            BufferedInputStream inStream = null;
            InputStream in = null;
            
            try {
                String url = "http://127.0.0.1:8080" + path;

                sender = new HttpRequestSender(url);

                in = sender.send();

                inStream = new BufferedInputStream(in);
            
                int code = sender.getResponseCode().intValue();
                if (code != 200)
                    fail(readStream(inStream));
                            
                assertTrue(code == 200);

                /*
                int bSize = 1000000;
                byte[] bytes = new byte[bSize];
                int nRead = inStream.read(bytes, 0, bSize);
                int a = 1;
                */
                
                Builder builder = ExampleMsg.newBuilder();
                
                PBStreamReader streamReader = new PBStreamReader();
                
                List<ExampleMsg> messages = streamReader.read(inStream, builder);
                assertNotNull(messages);
                assertTrue(messages.size() > 0);
                
            } catch (Throwable t) {
                fail(t.getMessage());
            } finally {
                try {
                    if (sender != null) {
                        sender.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                    if (inStream != null) {
                        inStream.close();
                    }
                } catch (IOException e) {
                }
            }
        }
    }
    
    public static boolean isOnline() {
   
        String urlString = "http://localhost:8080/";

        boolean isOnline = false;

        HttpURLConnection c = null;

        try {

            URL url = new URL(urlString);

            c = (HttpURLConnection) url.openConnection();

            c.setConnectTimeout(3000);

            c.setReadTimeout(3000);

            c.setRequestMethod("GET");

            c.setDoOutput(true);

            c.connect();

            Integer responseCode = (Integer.valueOf(c.getResponseCode()));

            if (responseCode.intValue() == 200)
                isOnline = true;

        } catch (IOException e) {

        } finally {
            if (c != null)
                c.disconnect();
        }

        return isOnline;
    }

    private String readStream(BufferedInputStream inStream) throws IOException {
        
        StringBuffer sb = new StringBuffer();
        int bf = 256;
        byte[] bytes = new byte[bf];
        while (inStream.read(bytes) != -1) {
            String line = new String(bytes, "UTF-8");
            sb.append(line);
        }
        
        return sb.toString();
    }
}
