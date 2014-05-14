package com.climbwithyourfeet.services.serveGPB;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.repackaged.com.google.common.base.Charsets;
import com.google.appengine.tools.development.RequestEndListener;
import com.google.appengine.tools.development.RequestThreadFactory;
import com.google.apphosting.api.ApiProxy;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * see http://googleappengine.googlecode.com/svn-history/trunk/java/src/main/com/google/appengine/tools/development/LocalEnvironment.java
 * and examples within
 * 
 * @author nichole
 */
public class TestEnvironment implements ApiProxy.Environment {
 
    public static final String API_CALL_SEMAPHORE = "com.google.appengine.tools.development.api_call_semaphore";

    private static final String REQUEST_THREAD_FACTORY_ATTR =
        "com.google.appengine.api.ThreadManager.REQUEST_THREAD_FACTORY";

    private static final String BACKGROUND_THREAD_FACTORY_ATTR =
        "com.google.appengine.api.ThreadManager.BACKGROUND_THREAD_FACTORY";

    private final Collection<RequestEndListener> requestEndListeners;

    protected final ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();

    static final Pattern APP_ID_PATTERN = Pattern.compile("([^:.]*)(:([^:.]*))?(.*)?");
    
    private static final String APPS_NAMESPACE_KEY =
        NamespaceManager.class.getName() + ".appsNamespace";

    public static final String INSTANCE_ID_ENV_ATTRIBUTE = "com.google.appengine.instance.id";

    private final String moduleId = "default";//WebModule.DEFAULT_MODULE_NAME

    /**
     * The name of an {@link #getAttributes() attribute} that contains a
     * (String) unique identifier for the curent request.
     */
    public static final String REQUEST_ID =
        "com.google.appengine.runtime.request_log_id";

    /**
     * The name of an {@link #getAttributes() attribute} that contains a a
     * {@link Date} object representing the time this request was started.
     */
    public static final String START_TIME_ATTR =
        "com.google.appengine.tools.development.start_time";

    /**
     * The name of an {@link #getAttributes() attribute} that contains a {@code
     * Set<RequestEndListener>}. The set of {@link RequestEndListener
     * RequestEndListeners} is populated by from within the service calls. The
     * listeners are invoked at the end of a user request.
     */
    public static final String REQUEST_END_LISTENERS =
        "com.google.appengine.tools.development.request_end_listeners";
    
    /**
     * The name of an {@link #getAttributes() attribute} that contains the {@link
     * javax.servlet.http.HttpServletRequest} instance.
     */
    public static final String HTTP_SERVLET_REQUEST =
        "com.google.appengine.http_servlet_request";

    /**
     * For historical and probably compatibility reasons dev appserver assigns
     * all versions a minor version of 1.
     */
    private static final String MINOR_VERSION_SUFFIX = ".1";

    /**
     * In production, this is a constant that defines the
     * {@link #getAttributes() attribute} name that contains the hostname on
     * which the default version is listening. In the local development server,
     * the {@link #getAttributes() attribute} contains the listening port in
     * addition to the hostname, and is the one and only frontend app instance
     * hostname and port.
     */
    public static final String DEFAULT_VERSION_HOSTNAME =
        "com.google.appengine.runtime.default_version_hostname";
    
     /**
     * Instance number for a main instance.
     * <p>
     * Clients depend on this literal having the value -1 so do not change this
     * value without making needed updates to clients.
     */
    public static final int MAIN_INSTANCE = -1;

    private static AtomicInteger requestID = new AtomicInteger();

    public TestEnvironment() {

        //NamespaceManager.set("");

        requestEndListeners =
            Collections.newSetFromMap(new ConcurrentHashMap<RequestEndListener, Boolean>(10));

        /*attributes.put(REQUEST_ID, generateRequestId());
         attributes.put(REQUEST_LOG_ID, generateRequestLogId());
         attributes.put(REQUEST_END_LISTENERS, requestEndListeners);
         attributes.put(START_TIME_ATTR, new Date());*/

        attributes.put(REQUEST_THREAD_FACTORY_ATTR, new RequestThreadFactory());
        attributes.put(REQUEST_ID, generateRequestId());
        attributes.put(REQUEST_END_LISTENERS, requestEndListeners);
        attributes.put(START_TIME_ATTR, new Date());
        //attributes.put(BACKGROUND_THREAD_FACTORY_ATTR, new BackgroundThreadFactory(appId,
        //    moduleName, majorVersionId));
    }

    public String getAppId() {
        return "Unit Tests";
    }

    public String getVersionId() {
        return "1.0";
    }

    /*public String getRequestNamespace() {
     return "gmail.com";
     }*/
    @SuppressWarnings("deprecation")
    public String getRequestNamespace() {
        return "";
        //return NamespaceManager.get();
    }

    public String getAuthDomain() {
        return "gmail.com";
    }

    public boolean isLoggedIn() {
        return false;
    }

    public String getEmail() {
        return "";
    }

    public boolean isAdmin() {
        return false;
    }

    public Map getAttributes() {
        return attributes;
    }

    @Override
    public long getRemainingMillis() {
        return 30000;
    }

    @Override
    public String getModuleId() {
      return moduleId;
    }
    
    /**
     * Generates a unique request ID using the same algorithm as the Python dev
     * appserver. It is similar to the production algorithm, but with less
     * embedded data. The primary goal is that the IDs be sortable by timestamp,
     * so the initial bytes consist of seconds then microseconds packed in
     * big-endian byte order. To ensure uniqueness, a hash of the incrementing
     * counter is appended. Hexadecimal encoding is used instead of base64 in
     * order to preserve comparison order.
     */
    private String generateRequestId() {
        try {
            ByteBuffer buf = ByteBuffer.allocate(12);

            long now = System.currentTimeMillis();
            buf.putInt((int) (now / 1000));
            buf.putInt((int) ((now * 1000) % 1000000));

            String nextID = new Integer(requestID.getAndIncrement()).toString();
            byte[] hashBytes =
                MessageDigest.getInstance("SHA-1").digest(
                nextID.getBytes(Charsets.unsafeDefaultCharset()));
            buf.put(hashBytes, 0, 4);

            return String.format("%x", new BigInteger(buf.array()));
        } catch (Exception e) {
            return "";
        }
    }
    
    public void callRequestEndListeners() {
        for (RequestEndListener listener : requestEndListeners) {
            try {
                listener.onRequestEnd(this);
            } catch (Exception ex) {

                System.err.println(
                    "Exception while attempting to invoke RequestEndListener " + listener.getClass()
                    + ": " + ex.getMessage());
            }
        }
        requestEndListeners.clear();
    }
}
