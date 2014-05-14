package com.climbwithyourfeet.services.serveGPB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.mortbay.http.HttpRequest;

public class MockHttpServletRequest implements HttpServletRequest {

    private Map<String, List<String> > parameters = new HashMap<String, List<String> >();

    private Map<String, Object> attributes = new HashMap<String, Object>();
    
    private Hashtable<String, String> headers = new Hashtable<String, String>();
    
    private Logger log = Logger.getLogger(MockHttpServletRequest.class.getName());

    private String method = HttpRequest.__GET;

    protected String requestURI = "";

    // default application/x-www-form-urlencoded
    protected String contentType = "text/plain";

    protected String characterEncoding = "UTF-8";
    
    protected String body = "";

    protected String scheme = null;

    //protected String queryString = "";
    //public void setQueryString(String str) {
    //    this.queryString = str;
    //}

    public MockHttpServletRequest() {
        log.info("MockHttpServletRequest");
    }

    public MockHttpServletRequest(Map<String, String> params) {

        log.info("MockHttpServletRequest");

        Iterator<Entry <String, String> > iter = params.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, String> entry = iter.next();
            String key = entry.getKey();
            String value = entry.getValue();
            List<String> list = new ArrayList<String>();
            list.add(value);
            parameters.put(key, list);
        }
    }

    @Override
    public String getParameter(String name) {
        List<String> values = parameters.get(name);
        return ((values != null) && (values.size() > 0)) ? values.get(0) : null;
    }
    
    public void _setHeader(String key, String value) {
        headers.put(key, value);
    }

    @Override
    public Enumeration getParameterNames() {
        Hashtable<String, List<String> > ht = new Hashtable<String, List<String> >();
        Iterator<Entry<String, List<String> > > iter = parameters.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, List<String> > entry = iter.next();
            ht.put(entry.getKey(), entry.getValue());
        }
        return ht.keys();
    }

    @Override
    public String[] getParameterValues(String name) {
        List<String> values = parameters.get(name);
        if ((values != null) && (values.size() > 0)) {
            return values.toArray(new String[values.size()]);
        } else {
            return null;
        }
    }

    @Override
    public Map getParameterMap() {
        return parameters;
    }

    // helper method not part of servlet spec
    public void setContentType(String type) {
        this.contentType = type;
    }
    
    public void setScheme(String s) {
        this.scheme = s;
    }
    /*  convenience setters for testing.  not part of spec */
    public void setBody(String str) {
        if (str != null) {
            this.body = str;
        }
    }
    
    // helper method not part of servlet spec
    public void setRequestURI(String theRequestURI) {
        this.requestURI = theRequestURI;
    }

    @Override
    public String getAuthType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Cookie[] getCookies() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getDateHeader(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    public String addHeader(String name, String value) {
        return headers.put(name, value);
    }

    @Override
    public Enumeration getHeaders(String name) {
        Hashtable<String, String > ht = new Hashtable<String, String >();
        Iterator<Entry<String, String> > iter = headers.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, String > entry = iter.next();
            ht.put(entry.getKey(), entry.getValue());
        }
        return ht.elements();
    }

    @Override
    public Enumeration getHeaderNames() {
        return headers.keys();
    }

    @Override
    public int getIntHeader(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // not part of the spec:
    public void setMethod(String theMethod) {
        this.method = theMethod;
    }

    @Override
    public String getMethod() {
        return method;
    }

    /*• Context Path: The path prefix associated with the ServletContext that this servlet is a part of.
     *      If this context is the “default” context rooted at the base of the Web server’s URL name space,
     *      this path will be an empty string. Other- wise,
     *      if the context is not rooted at the root of the server’s name space,
     *      the path starts with a’/’ character but does not end with a’/’ character.
       • Servlet Path: The path section that directly corresponds to the mapping which
     *      activated this request. This path starts with a’/’ character except in the case
     *      where the request is matched with the ‘/*’ pattern, in which case it is an empty string.
       • PathInfo: The part of the request path that is not part of the Context Path or the Servlet Path.
       *    It is either null if there is no extra path, or is a string with a leading ‘/’.
       *
       * Examples:
       *
       * Request Path                                 Path Elements
       *     /catalog/lawn/index.html                  ContextPath: /catalog
       *                                                            ServletPath: /lawn
       *                                                            PathInfo: /index.html
       *
              /catalog/garden/implements/           ContextPath: /catalog
                                                                    ServletPath: /garden
                                                                    PathInfo: /implements/

              /catalog/help/feedback.jsp              ContextPath: /catalog
                                                                    ServletPath: /help/feedback.jsp
                                                                    PathInfo: null
       */

    @Override
    public String getPathInfo() {
        if (requestURI == null) {
            return null;
        }

        int i0 = requestURI.lastIndexOf("/");
        if (i0 == -1) {
            if (requestURI.endsWith("html")) {
                return requestURI;
            } else {
                return null;
            }
        }
        //    /asdf/
        //    01234
        if (i0 == (requestURI.length() - 1)) {
            String substr = requestURI.replaceAll("/", "");
            return substr;
        }
        String substr = requestURI.substring(i0);
        if (substr.endsWith(".jsp")) {
            return null;
        }
        return substr;
    }
    
    @Override
    public String getCharacterEncoding() {
        String enc = headers.get("Charset");
        if (enc == null) {
            enc = characterEncoding;
        }
        return enc;
    }

    @Override
    public void setCharacterEncoding(String charset) throws UnsupportedEncodingException {
        if (charset != null) {
            headers.put("Charset", charset);
            characterEncoding = charset;
        }
    }

    @Override
    public int getContentLength() {
        String len = headers.get("Content-Length");
        return (len != null) ? Integer.valueOf(len) : -1;
    }

    @Override
    public String getContentType() {
        return headers.get("Content-Type");
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {

        ServletInputStream sis = new ServletInputStream() {

            protected int bodyReadPosition = 0;

            @Override
            public int read() throws IOException {
                if (bodyReadPosition >= body.length()) {
                    return -1;
                }
                int ret = body.codePointAt(bodyReadPosition);
                bodyReadPosition++;
                return ret;
            }
        };

        return sis;
    }

    @Override
    public String getPathTranslated() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getContextPath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getQueryString() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRemoteUser() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isUserInRole(String role) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRequestedSessionId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    @Override
    public StringBuffer getRequestURL() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getServletPath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpSession getSession(boolean create) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpSession getSession() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isRequestedSessionIdValid() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration getAttributeNames() {
        return null;
    }

    @Override
    public String getProtocol() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getServerName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getServerPort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BufferedReader getReader() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRemoteAddr() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRemoteHost() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Enumeration getLocales() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSecure() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRealPath(String path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getRemotePort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLocalName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLocalAddr() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getLocalPort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
