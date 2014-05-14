package com.climbwithyourfeet.services.serveGPB;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
partial implementation of a mock ServletConfig for test purposes
*/
public class MockServletConfig implements ServletConfig {

	protected final String servletName;
	
	protected final ServletContext servletContext;
	
	protected Map<String, List<String> > parameters = new HashMap<String, List<String> >();

	public MockServletConfig(ServletContext ctx, String servletName) {
		this.servletName = servletName;
		servletContext = ctx;
	}
	
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
	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public String getServletName() {
		return servletName;
	}

	///     implement as needed
}
