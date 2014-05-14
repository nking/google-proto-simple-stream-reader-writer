package com.climbwithyourfeet.services.serveGPB;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.apphosting.api.ApiProxy;
import junit.framework.TestCase;

/**
 from http://code.google.com/appengine/docs/java/howto/unittesting.html
 * 
 * @author nichole
 */
public class LocalServiceTestCase extends TestCase {
    
    protected final LocalServiceTestHelper helper1 =
        new LocalServiceTestHelper();
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        helper1.setUp();
        ApiProxy.setEnvironmentForCurrentThread(new TestEnvironment());
        ApiProxy.setDelegate(LocalServiceTestHelper.getApiProxyLocal());
    }

    @Override
    public void tearDown() throws Exception {
        try {
            ApiProxy.setDelegate(null);
            ApiProxy.setEnvironmentForCurrentThread(null);
            helper1.tearDown();
            super.tearDown();
        } catch (Throwable t) {
        }
    }
    
    public void test(){}
}
