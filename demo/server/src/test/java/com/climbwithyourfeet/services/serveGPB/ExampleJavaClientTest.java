package com.climbwithyourfeet.services.serveGPB;

import com.climbwithyourfeet.proto.ExampleMessageProto;
import com.climbwithyourfeet.proto.ExampleMessageProto.ExampleMsg;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.api.urlfetch.FetchOptions;
import java.util.List;
import java.net.URL;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import com.google.protobuf.GeneratedMessage.Builder;
import com.google.code.proto.streamio.*;
import com.google.protobuf.CodedInputStream;
import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *
 * @author nichole
 */
public class ExampleJavaClientTest extends LocalServiceTestCase {

    protected Logger log = Logger.getLogger(this.getClass().getName());
    
    private boolean isOnline = false;

    public ExampleJavaClientTest() {
        super();
    }

    public void setUp() throws Exception {

        super.setUp();

        isOnline = localServerIsActive();
        
        log.log(Level.FINE, "isonline={0}", 
            new Object[]{Boolean.valueOf(isOnline)});
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetDefaultMessages() throws Exception {
        String path = "/gpb";
        
        assertMessages(path);
    }
    
    public void testGetCustomDelimiterMessages() throws Exception {
        
        String path = "/gpbplus";
        
        assertMessages(path);
    }
    
    public void testGetDefaultMessagesWith1252Encoding() throws Exception {
        
        String ct = URLEncoder.encode("text/plain");
        
        String et = URLEncoder.encode("Windows-1252");
        
        String path = "/gpb?et=" + et + "&" + ct;
        
        assertMessages(path);
    }

    public void assertMessages(String pathAndQuery) throws Exception {

        if (isOnline) {
            
            String urlStr = "http://127.0.0.1:8080" + pathAndQuery;

            URL url = new URL(urlStr);

            URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
            FetchOptions fetchOptions
                = com.google.appengine.api.urlfetch.FetchOptions.Builder.followRedirects().disallowTruncate();
            fetchOptions.setDeadline(10.0);

            HTTPRequest httpRequest = new HTTPRequest(url, HTTPMethod.GET, fetchOptions);

            HTTPResponse httpResponse = urlFetchService.fetch(httpRequest);

            byte[] contentBytes = httpResponse.getContent();

            String content = (contentBytes != null) ? new String(contentBytes, "UTF-8") : "";

            int responseCode = httpResponse.getResponseCode();

            if (responseCode != 200) {
                fail(content);
            }

            assertTrue(responseCode == 200);

            log.log(Level.FINE, "read serialized message: {0}", 
                new Object[]{new String(contentBytes)});
            
            Builder builder = ExampleMsg.newBuilder();

            ByteArrayInputStream in = null;

            try {
                in = new ByteArrayInputStream(contentBytes);
                
                if (pathAndQuery.contains("gpbplus")) {
                    
                    // reading messages that were generated with custom delimiters
                    
                    PBStreamReader streamReader = new PBStreamReader();

                    List<ExampleMsg> messages = streamReader.read(in, builder);
                    assertNotNull(messages);
                    assertTrue(messages.size() == 2);
                
                } else {
                    
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
                }

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
    }
    
    /**
     * check that address http://localhost:8080 is listening and reachable
     *
     * @return
     */
    public static boolean localServerIsActive() {

        String urlString = "http://127.0.0.1:8080/";

        return haveConnection(urlString);
    }

     public static boolean haveConnection(String urlStr) {

       boolean haveNetworkConnection = false;

       try {
           URL url = new URL(urlStr);

           URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
           FetchOptions fetchOptions =
               com.google.appengine.api.urlfetch.FetchOptions.Builder.followRedirects().disallowTruncate();
           fetchOptions.setDeadline(10.0);

           HTTPRequest httpRequest = new HTTPRequest(url, HTTPMethod.GET, fetchOptions);

           HTTPResponse httpResponse = urlFetchService.fetch(httpRequest);

           int responseCode = httpResponse.getResponseCode();

           if (responseCode == 200 || responseCode == 403) {

               haveNetworkConnection = true;
           }
                      
       } catch (IOException e) {

       }

       return haveNetworkConnection;
   }
    
}
