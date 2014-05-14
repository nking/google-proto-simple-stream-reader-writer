package com.climbwithyourfeet.services.serveGPB;

import java.util.logging.Logger;
import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author nichole
 */
public class UtilTest extends TestCase {
    
    protected Logger log = Logger.getLogger(this.getClass().getName());
    
    public UtilTest() {
        super();
    }

    public void setUp() throws Exception {
        super.setUp();
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of replaceJavascriptHazards method, of class Util.
     */
    @Test
    public void testReplace() {
        
        log.info("replaceJavascriptHazards");
        
        String str = "ASDF\"G'H\nJKL\r";
        
        String expResult = "ASDF&#34;G&#39;HJKL";
        
        String result = Util.replace(str);
        
        assertEquals(expResult, result);
    }
    
    /**
     * Test of replaceJavascriptHazards method, of class Util.
     */
    @Test
    public void testReplaceJavascriptHazards() {
        
        log.info("replaceJavascriptHazards");
        
        String str = "A�SDF\"G'H\nJKL\r";
        
        String expResult = "A&#65533;SDF&#34;G&#39;HJKL";
        
        String result = Util.replaceJavascriptHazards(str);
        
        assertEquals(expResult, result);
    }

    /**
     * Test of replaceUnsafeForSomeBrowsers method, of class Util.
     */
    @Test
    public void testReplaceUnsafeForSomeBrowsers() {
        
        log.info("replaceUnsafeForSomeBrowsers");
        
        String str = "ASDF�GH឵JKL឴M؁";
        
        String expResult = "ASDF&#65533;GH&#6069;JKL&#6068;M&#1537;";
        
        String result = Util.replaceUnsafeForSomeBrowsers(str);
        
        assertEquals(expResult, result);
    }
    
}
