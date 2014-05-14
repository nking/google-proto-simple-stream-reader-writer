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
        
        str = replaceUnsafeForSomeBrowsers(str);
        
        str = replace(str);

        return str;
    }
     
     /**
     * replace content in text which may break javascript parsing and deserialization
     * 
     * @param str
     * @return
     */
     static String replace(String str) {
        str = str.replaceAll("'", "&#39;");
        str = str.replaceAll("\"", "&#34;");
        str = str.replaceAll("\n", "");
        str = str.replaceAll("\r", "");

        return str;
    }
     
     /**
      * replace characters that are unsafe in some browsers as noted by
      * jslint http://www.jslint.com/lint.html
      * 
      * @param str
      * @return 
      */
     static String replaceUnsafeForSomeBrowsers(String str) {
        //unsafe characters in some browsers:
         char[] repl = new char[] {
             '\u0000', '\u0001', '\u0007', '\u0008', '\u0009', '\u00ad', 
             '\u0600', '\u0601', '\u0602', '\u0603', '\u0604',
             '\u070f', '\u17b4', '\u17b5',
             '\u200c', '\u200d', '\u200e', '\u200f',
             '\u2028', '\u2029', '\u202a', '\u202b', '\u202c', '\u202d',
             '\u202e', '\u202f', 
             '\u2060', '\u2061', '\u2062', '\u2063', '\u2064', '\u2065',
             '\u2066', '\u2067', '\u2068', '\u2069', '\u206a', '\u206b',
             '\u206c', '\u206d', '\u206e', '\u206f',
             '\ufeff',
             '\ufff0', '\ufff1', '\ufff2', '\ufff3', '\ufff4', '\ufff5',
             '\ufff6', '\ufff7', '\ufff8', '\ufff9', '\ufffa', '\ufffb',
             '\ufffc', '\ufffd', '\ufffe', '\uffff'
         };
         
         String[] replEntities = new String[] {
             "&#0;", "&#1;", "&#7;", "&#8;", "&#9;", "&shy;", 
             "&#1536;", "&#1537;", "&#1538;", "&#1539;", "&#1540;",
             "&#1807;", "&#6068;", "&#6069;",
             "&zwnj;", "&zwj;", "&lrm;", "&rlm;",
             "&#8232;", "&#8233;", "&#8234;", "&#8235;", "&#8236;", "&#8237;",
             "&#8238;", "&#8233;",
             "&#8288;", "&#8289;", "&#8290;", "&#8291;", "&#8292;", "&#8293;",
             "&#8294;", "&#8295;", "&#8296;", "&#8297;", "&#8298;", "&#8299;",
             "&#8300;", "&#8301;", "&#8302;", "&#8303;",
             "&#65279;",
             "&#65520;", "&#65521;", "&#65522;", "&#65523;", "&#65524;", "&#65525;",
             "&#65526;", "&#65527;", "&#65528;", "&#65529;", "&#65530;", "&#65531;",
             "&#65532;", "&#65533;", "&#65534;", "&#65535;",
         };
         
         StringBuilder sb = new StringBuilder(str);
         boolean changed = false;
         for (int i = 0; i < sb.length(); i++) {
             int cint = sb.charAt(i);
             for (int j = 0; j < repl.length; j++) {
                 int rint = repl[j];
                 if (cint == rint) {
                     changed = true;
                     String cs = replEntities[j];
                     int len = cs.length();
                     if (len > 1) {
                         sb.deleteCharAt(i);
                         sb.insert(i, cs);
                     } else {
                         sb.replace(i, i + len, cs);
                     }
                 }
             }
         }
         
         return changed ? sb.toString() : str;
     }
}
