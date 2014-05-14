package com.climbwithyourfeet.services.serveGPB;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
partial implementation of a mock ServletContext for test purposes
*/
public class MockServletContext implements ServletContext {

	Map<String, Object> attributes = new HashMap<String, Object>();
	
	private Map<String, List<String> > parameters = new HashMap<String, List<String> >();
	
	public MockServletContext(Map<String, List<String>> params) {
		if (params != null) {
			this.parameters = params;
		}
	}
	public MockServletContext(){}
	
	@Override
	public String getInitParameter(String param) {
		List<String> values = parameters.get(param);
        return ((values != null) && (values.size() > 0)) ? values.get(0) : null;
	}
	@Override
	public Enumeration getInitParameterNames() {
		Hashtable<String, List<String> > ht = new Hashtable<String, List<String> >();
        Iterator<Entry<String, List<String> > > iter = parameters.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, List<String> > entry = iter.next();
            ht.put(entry.getKey(), entry.getValue());
        }
        return ht.elements();
	}
	
	@Override
	public Object getAttribute(String attr) {
		return attributes.get(attr);
	}

	@Override
	public Enumeration getAttributeNames() {
		return null;
	}
	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}
	@Override
	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}
	
	///     implement as needed
	
	@Override
	public ServletContext getContext(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMimeType(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set getResourcePaths(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletContextName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration getServletNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration getServlets() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void log(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void log(Exception arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void log(String arg0, Throwable arg1) {
		// TODO Auto-generated method stub

	}

}
