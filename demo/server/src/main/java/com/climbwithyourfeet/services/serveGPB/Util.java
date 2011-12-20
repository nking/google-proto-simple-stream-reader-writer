package com.climbwithyourfeet.services.serveGPB;

/**
 *
 * @author nichole
 */
public class Util {

    /**
     * replace content in text which may break javascript parsing and deserialization
     * 
     * @param str
     * @return
     */
     public static String replaceJavascriptHazards(String str) {
        str = str.replaceAll("'", "&#39;");
        str = str.replaceAll("\"", "&#34;");
        str = str.replaceAll("\n", "");
        str = str.replaceAll("\r", "");

        return str;
    }
}
