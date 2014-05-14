package com.climbwithyourfeet.services.serveGPB;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author nichole
 */
public class MockHttpServletResponse implements HttpServletResponse {

    private Map<String, List<String>> parameters = new HashMap<String, List<String>>();

    private Hashtable<String, String> headers = new Hashtable<String, String>();

    protected int responseCode = -1;

    protected String responseMessage = null;

    protected byte[] content = new byte[0];
    
    protected int contentOffset = 0;
    
    protected String contentType = "text/plain";
    
    protected String characterEncoding = "UTF-8";

    public MockHttpServletResponse() {
    }

    @Override
    public void addCookie(Cookie cookie) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    @Override
    public String encodeURL(String url) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String encodeRedirectURL(String url) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String encodeUrl(String url) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String encodeRedirectUrl(String url) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
    	responseCode = sc;
    	responseMessage = msg;
        content = msg.getBytes();
        contentOffset = content.length;
    }

    @Override
    public void sendError(int sc) throws IOException {
    	responseCode = sc;
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setDateHeader(String name, long date) {
        addHeader(name, (new Date(date)).toString() );
    }

    @Override
    public void addDateHeader(String name, long date) {
        headers.put(name, Long.toString(date));
    }

    @Override
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        headers.put(name, Integer.valueOf(value).toString());
    }

    @Override
    public void addIntHeader(String name, int value) {
    	headers.put(name, Integer.valueOf(value).toString());
    }

    @Override
    public void setStatus(int sc) {
    	responseCode = sc;
    }

    @Override
    public void setStatus(int sc, String sm) {
    	responseCode = sc;
    	responseMessage = sm;
        content = sm.getBytes();
        contentOffset = content.length;
    }

    @Override
    public String getCharacterEncoding() {
        return "UTF-8";
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    // helper method
    protected void increaseContentArraySize(int totSize) {
        content = Arrays.copyOf(content, totSize);
    }
    // helper method
    public byte[] getContent() {
        return Arrays.copyOf(content, contentOffset);
    }
    
    /*
     * convenience methods for testing
     */
    public String getBody() {
        return new String(getBody());
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        
        ServletOutputStream sout = new ServletOutputStream() {
            PrintStream out = System.out;
            int t0 = 0;
            @Override
            public void write(int b) throws IOException {
            
System.out.println(t0 + " byte=" + b);
t0++;
                out.write(b);

                byte bb = (byte)b;
                byte[] bbb = new byte[] {bb};

                if (content.length < (content.length + bbb.length)) {
                    increaseContentArraySize(content.length + bbb.length);
                }

                System.arraycopy(bbb, 0, content, contentOffset, bbb.length);
                contentOffset += bbb.length;
            }

            @Override
            public void write(byte[] b) throws IOException {
for (byte bb : b) {
System.out.println(t0 + " byte=" + bb);
t0++;
}
                if (content.length < (content.length + b.length)) {
                    increaseContentArraySize(content.length + b.length);
                }

                // arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
                System.arraycopy(b, 0, content, contentOffset, b.length);
                contentOffset += b.length;
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
for (int i = off; i < off + len; i++) {
System.out.println(t0 + " byte=" + b[i]);
t0++;
}
                if (content.length < (content.length + b.length)) {
                    increaseContentArraySize(content.length + b.length);
                }

                // arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
                System.arraycopy(b, off, content, contentOffset, len);
                contentOffset += len;
            }

        };
        return sout;
    }

    private class PrintWriterToContents extends PrintWriter {

        public PrintWriterToContents(OutputStream outStream) {
            super(outStream);
        }
        public PrintWriterToContents(File file) throws FileNotFoundException {super(file);}
        public PrintWriterToContents(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {super(file, csn);}
        public PrintWriterToContents(OutputStream out, boolean autoFlush) {super(out, autoFlush);}
        public PrintWriterToContents(String fileName) throws FileNotFoundException {super(fileName);}
        public PrintWriterToContents(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {super(fileName, csn);}
        public PrintWriterToContents(Writer out) {super(out);}
        public PrintWriterToContents(Writer out, boolean autoFlush) {super(out, autoFlush);}
        @Override
        public void write(String s) {
            super.write(s);
            if (content.length < (content.length + s.length())) {
                increaseContentArraySize(content.length + s.length());
            }
            try {
                byte[] b = s.getBytes(characterEncoding);
                // arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
                System.arraycopy(b, 0, content, contentOffset, b.length);
                contentOffset += b.length;
            } catch (UnsupportedEncodingException e) {
            }
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        
        Writer out = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                if (content.length < (content.length + len)) {
                    increaseContentArraySize(content.length + len);
                }
                try {
                    String s = new String(cbuf, off, len);
                    byte[] b = s.getBytes(characterEncoding);
                    // arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
                    System.arraycopy(b, 0, content, contentOffset, b.length);
                    contentOffset += b.length;
                } catch (UnsupportedEncodingException e) {
                    throw new IOException(e);
                }
            }

            @Override
            public void flush() throws IOException {
            }

            @Override
            public void close() throws IOException {
            }
        };

        PrintWriter writer = new PrintWriter(out);
        return writer;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        if (charset != null) {
            headers.put("Charset", charset);
            characterEncoding = charset;
        }
    }
    @Override
    public void setContentLength(int len) {
        if (len > -1 ) {
            headers.put("Content-Length", Integer.toString(len));
        }
    }
    @Override
    public void setContentType(String type) {
        if (type != null) {
            headers.put("Content-Type", type);
            contentType = type;
        }
    }

    @Override
    public void setBufferSize(int size) {
    }

    @Override
    public int getBufferSize() {
        return 2048;
    }

    @Override
    public void flushBuffer() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void resetBuffer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCommitted() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLocale(Locale loc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public int getResponseCode() {
    	return responseCode;
    }
    public String getResponseMessage() {
        if (responseMessage == null) {
            byte[] b = Arrays.copyOf(content, contentOffset);
            responseMessage = new String(b);
        }
    	return responseMessage;
    }
}
