package com.climbwithyourfeet.services.serveGPB;

import com.climbwithyourfeet.proto.ExampleMessageProto;
import com.climbwithyourfeet.proto.ExampleMessageProto.ExampleMessages;
import com.climbwithyourfeet.proto.ExampleMessageProto.ExampleMsg;
import com.google.code.proto.streamio.*;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.GeneratedMessage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * serves gpb messages
 *
 * @author nichole
 */
public class GPBServlet extends HttpServlet {

    private final static long serialVersionUID = 6;

    private transient Logger log = null;

    private final List<ExampleMessageProto.ExampleMsg> messages = new ArrayList<ExampleMsg>();
   
    private List<String> sharedDomains = new ArrayList<String>();

    private String name1 = "a title with more than 128 characters to see if the string length decoded length from the preceding varint is only wrong in the javascript parser protobuf.js";
    private String value1 = "a value";
    private int code1 = 200;
    private String name2 = "a protocol message";
    private String value2 = "";
    private int code2 = 201;
      
    private ExampleMessages messagesGPB = null;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        
        log = Logger.getLogger(this.getClass().getName());
      
        initSharedDomains();
        
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
        
        ExampleMessages.Builder builderGPBMessages = ExampleMessages.newBuilder();
        builderGPBMessages.addAllMsg(messages);
        messagesGPB = builderGPBMessages.build();
    }
    
    private synchronized void initSharedDomains() {
        if (sharedDomains.isEmpty()) {
            String appEngineEnv = System.getProperty("com.google.appengine.runtime.environment");
            if (appEngineEnv != null && appEngineEnv.equalsIgnoreCase("Development")) {
                sharedDomains.add("http://localhost:8080");
                sharedDomains.add("http://localhost:8081");
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        log.log(Level.FINE, "{0}", req.getRequestURI());
        
        String useDelimiters = req.getParameter("useDelimiters");
        
        BufferedOutputStream out = null;
        
        try {
            if ((useDelimiters != null) && useDelimiters.equalsIgnoreCase("true")) {
                
                resp.setContentType("application/octet-stream");
                resp.setCharacterEncoding("UTF-8");
                        
                PBWireSignedByteMarkerHelper pbh = new PBWireSignedByteMarkerHelper();
                
                int expectedSize = pbh.estimateTotalContentLength(messages);
                
                resp.setContentLength(expectedSize);
                
                addCORSHeaders(resp, req.getHeader("Origin"));
                
                out = new BufferedOutputStream(resp.getOutputStream(), 1024);

                PBStreamWriter.writeToStream(out, messages);
                    
                log.log(Level.INFO, "sent {0} messages", messages.size());
                
            } else {
                
                resp.setContentType("application/octet-stream");
                resp.setCharacterEncoding("UTF-8");
                                        
                int expectedSize = messagesGPB.getSerializedSize();
                
                resp.setContentLength(expectedSize);
                
                addCORSHeaders(resp, req.getHeader("Origin"));
                
                out = new BufferedOutputStream(resp.getOutputStream(), 1024);

                CodedOutputStream cos = CodedOutputStream.newInstance(out);
                messagesGPB.writeTo(cos);
                cos.flush();
                out.flush();

                log.log(Level.INFO, "sent {0} messages", messages.size());
            }

        } catch (NumberFormatException e) {

            resp.setStatus(404);

        } catch (Throwable t) {

            log.severe(t.getMessage());
 
            t.printStackTrace();

            resp.setStatus(500);

        } finally {
            if (out != null)
                out.close();
            
            log.log(Level.FINE, "done writing");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        doGet(req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) 
        throws ServletException, IOException {
                
        addCORSHeaders(resp, req.getHeader("Origin"));
    }
    
    protected void addCORSHeaders(HttpServletResponse resp, String origin) 
        throws ServletException, IOException {

        if (origin == null) {
            return;
        }
                    
        log.info("origin = " + origin);
     
        if (sharedDomains.contains(origin.trim())) {
                       
            log.info("adding CORS headers to request");
            
            resp.setHeader("Access-Control-Allow-Headers", "x-requested-with");
            resp.setHeader("Access-Control-Allow-Origin",  origin);
            resp.setHeader("Access-Control-Allow-Methods", "GET,OPTIONS");
            resp.setHeader("Access-Control-Allow-Headers", "all");
            resp.setHeader("Access-Control-Max-Age",       "86400");
        }
    }
    
    public int estimateTotalContentLength(List<? extends GeneratedMessage> messages) {
        
        int sum = 0;
        
        for (GeneratedMessage message : messages) {
        
            int messageSize = message.getSerializedSize();
                        
            sum += messageSize;
        }
        
        return sum;
    }
}
