package com.climbwithyourfeet.services.serveGPB;

import com.climbwithyourfeet.proto.ExampleMessageProto;
import com.climbwithyourfeet.proto.ExampleMessageProto.ExampleMessages;
import com.climbwithyourfeet.proto.ExampleMessageProto.ExampleMsg;
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
 * Serves Google Protocol Buffer Messages.  Default settings serve the Google Protocol
 * Buffer messages with their built-in delimiters and content-type ""octet-stream" and
 * character encoding "UTF-8".
 *
 * Servlet accepts the following optional parameters:
 * <code>
 *   useComposite = true
 *   ct = text/plain
 *   ct = octet-stream
 *   ec = UTF-7
 *   ec = UTF-8
 *   ec = ISO-8859-1
 *   ec = Windows-1252
 *   u  = true
 *        to send unicode to distinguish between Windows1252 and ISO8859-1
 *</code>
 * 
 * @author nichole
 */
public class GPBServlet extends HttpServlet {

    private final static long serialVersionUID = 6;

    private transient Logger log = null;

    private final List<ExampleMessageProto.ExampleMsg> messages = new ArrayList<ExampleMsg>();
    private final List<ExampleMessageProto.ExampleMsg> messagesWithUnicode = new ArrayList<ExampleMsg>();

    private List<String> sharedDomains = new ArrayList<String>();

    /*
    ISO-8859-1 differs from Windows-1252 in code points 128-159 (0x80-0x9F)
    
    dec  hex   w-1252     unicode    UTF-8 bytes      NCR
                 hex        hex
    
    128	  80	U+00A4	   U+20AC	   E2  82  AC	 &#8364;  €	 &euro;	Euro Sign
        This symbol is undefined in ISO-8859-1.
    
    158   9E    U+00B8     U+017E      C5  BE        &#382;   ž  &#x17E;  Latin Small Letter Z With Caron
    
    */
    
    private String windows1252Char1 = "\u20AC";
    private String windows1252Char2 = "\u017E";
    
    private String name1 = "Wallaroo";
    private String value1 = "A Wallaroo is any of three closely related species of moderately large macropod, intermediate in size between the kangaroos and the wallabies.[Wikipedia]";
    private int code1 = 200;
    private String name2 = "Wallaby";
    private String value2 = "Any of various small or medium-sized kangaroos; often brightly colored.[Wordnet]";
    private int code2 = 201;

    private ExampleMessages messagesGPB = null;
    
    private ExampleMessages messagesGPBWithUnicode = null;

    @Override
    public void init(ServletConfig config) throws ServletException {

        log = Logger.getLogger(this.getClass().getName());

        initSharedDomains();

        // build individual messages
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
        // build a composite message composed of the above messages
        ExampleMessages.Builder builderGPBMessages = ExampleMessages.newBuilder();
        builderGPBMessages.addAllMsg(messages);
        messagesGPB = builderGPBMessages.build();
        
        
        // same messages, but now add 2 unicode characters to distinguish between encodings
        ExampleMsg.Builder msgUnicode = ExampleMsg.newBuilder();
        msgUnicode.setName(name1);
        msgUnicode.setValue(windows1252Char1 + " " + value1);
        msgUnicode.setCode(code1);
        messagesWithUnicode.add(msgUnicode.build());
        msgUnicode.clear();
        msgUnicode.setName(name2);
        msgUnicode.setValue(windows1252Char2 + " " + value2);
        msgUnicode.setCode(code2);
        messagesWithUnicode.add(msgUnicode.build());
        // build a composite message composed of the above messages
        ExampleMessages.Builder builderGPBMessagesUnicode = ExampleMessages.newBuilder();
        builderGPBMessagesUnicode.addAllMsg(messagesWithUnicode);
        messagesGPBWithUnicode = builderGPBMessages.build();
    }

    private synchronized void initSharedDomains() {
        if (sharedDomains.isEmpty()) {
            String appEngineEnv = System.getProperty("com.google.appengine.runtime.environment");
            if (appEngineEnv != null && appEngineEnv.equalsIgnoreCase("Development")) {
            
                sharedDomains.add("http://127.0.0.1:8080");
                sharedDomains.add("http://192.168.15.1:8080");
            
            } else {
                // add your CORS Domains
                //sharedDomains.add("http://yourdomaintoshareresourceswith");
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        log.log(Level.FINE, "{0}", req.getRequestURI());

        String useComposite = req.getParameter("useComposite");
        
        boolean useUnicode = (req.getParameter("u") != null) && 
            (req.getParameter("u").equalsIgnoreCase("true"));

        BufferedOutputStream out = null;

        String contentType = req.getParameter("ct");
        String encoding = req.getParameter("ec");

        log.info("req contentType=" + contentType + "  req encoding=" + encoding);

        boolean iso88591 = ((contentType != null) && (encoding != null)
            && contentType.equalsIgnoreCase("text/plain") && encoding.equalsIgnoreCase("ISO-8859-1"));

        boolean utf7text = ((contentType != null) && (encoding != null)
            && contentType.equalsIgnoreCase("text/plain") && encoding.equalsIgnoreCase("UTF-7"));

        boolean utf8text = ((contentType != null) && (encoding != null)
            && contentType.equalsIgnoreCase("text/plain") && encoding.equalsIgnoreCase("UTF-8"));

        boolean windows1252text = ((contentType != null) && (encoding != null)
            && contentType.equalsIgnoreCase("text/plain") && encoding.equalsIgnoreCase("Windows-1252"));

        boolean octetstream = ((contentType != null) && (encoding != null)
            && contentType.equalsIgnoreCase("octet-stream") && encoding.equalsIgnoreCase("UTF-8"));

        try {

            if (windows1252text) {
                resp.setContentType("text/plain");
                resp.setCharacterEncoding("Windows-1252");
                log.info("setting stream content-type to text/plain and character encoding to Windows-1252");
            } else if (utf7text) {
                resp.setContentType("text/plain");
                resp.setCharacterEncoding("UTF-7");
                log.info("setting stream content-type to text/plain and character encoding to UTF-7");
            } else if (utf8text) {
                resp.setContentType("text/plain");
                resp.setCharacterEncoding("UTF-8");
                log.info("setting stream content-type to text/plain and character encoding to UTF-8");
            } else if (iso88591) {
                resp.setContentType("text/plain");
                resp.setCharacterEncoding("ISO-8859-1"); /* needed for IE, most of their ajax expects text/plain */
                log.info("setting stream content-type to text/plain and character encoding to ISO-8859-1");
            } else if (octetstream) {
                resp.setContentType("application/octet-stream");
                resp.setCharacterEncoding("UTF-8");
                log.info("setting stream contenttype to octet-stream and encoding to UTF-8");
            } else {
                resp.setContentType("application/octet-stream");
                resp.setCharacterEncoding("UTF-8");
                log.info("setting stream contenttype to octet-stream and encoding to UTF-8");
            }

            resp.addHeader("Cache-Control", "no-cache, must-revalidate, max-age=0");

            addCORSHeaders(resp, req.getHeader("Origin"));

            if ((useComposite == null) || useComposite.equalsIgnoreCase("false")) {
                
                // generating w/ Google's built-in delimiters
                
                List<ExampleMessageProto.ExampleMsg> sendMessages;
                if (useUnicode) {
                    sendMessages = messagesWithUnicode;
                    log.log(Level.INFO, "sending messages w/ unicode characters");
                } else {
                    sendMessages = messages;
                }

                out = new BufferedOutputStream(resp.getOutputStream(), 1024);
                for (int i = 0; i < sendMessages.size(); i++) {
                    sendMessages.get(i).writeDelimitedTo(out);
                }

                resp.setStatus(200);

                log.log(Level.INFO, "sent {0} messages w/ built-in delimiters", sendMessages.size());

            } else {
                
                // generating composite messages, no delimiters

                ExampleMessages sendMessages;
                if (useUnicode) {
                    sendMessages = messagesGPBWithUnicode;
                    log.log(Level.INFO, "sending messages w/ unicode characters");
                } else {
                    sendMessages = messagesGPB;
                }
                
                out = new BufferedOutputStream(resp.getOutputStream(), 1024);

                log.info("***gpb composite via codedoutput w/ a single delimiter");
                sendMessages.writeDelimitedTo(out);

                resp.setStatus(200);

                log.log(Level.INFO, "sent {0} messages as a composite message w/ built-in delimiter", 
                    sendMessages.getMsgCount());
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
