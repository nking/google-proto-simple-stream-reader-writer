package com.climbwithyourfeet.services.serveGPB;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

/**
 * class to send HTTP Get or Post requests and receive a buffered reader for the
 * returned input stream
 * 
 * GoogleAppEngine environment uses an HTTP/1.1 compliant proxy.  The connection 
 * and perhaps read timeout are 5 seconds.
 * 
 * @author nichole
 */
public class HttpRequestSender {

    private String urlString = null;
    private Integer responseCode = null;
    private InputStream in = null;
    private HttpURLConnection c = null;
    private HttpsURLConnection cSSL = null;
    private int connectTimeoutInMillis = 15000;
    private int readTimeoutInMillis = 15000;

    private String responseMessage = null;
    
    /**
     * map of request parameters to set
     */
    private Map<String, String> parameterMap = new HashMap<String, String>();
    private boolean isPOST = false;

    public HttpRequestSender(String urlString) {

        this.urlString = urlString;
    }

    public void addParameter(String key, String value) {

        parameterMap.put(key, value);
    }

    public InputStream send() throws MalformedURLException, IOException, NoSuchAlgorithmException {

        return sendHTTP();
    }
    public InputStream sendPost() throws MalformedURLException, IOException, NoSuchAlgorithmException {
        this.isPOST = true;
        return sendHTTP();
    }

    private void readStream(InputStream in) throws IOException {
        
        BufferedReader reader = null;
        
        try {
        
            reader = new BufferedReader(new InputStreamReader(in));

            StringBuffer sb = new StringBuffer();

            String line = null;

            while ((line = reader.readLine()) != null) {

                sb.append(line);
            }

            this.responseMessage = sb.toString();

        } finally {
            if (reader != null)
                reader.close();
        }

    }

    private InputStream sendHTTP() throws MalformedURLException, IOException {

        URL url = null;

        try {

            String query = getQuery();

            if (isPOST || (query.length() == 0)) {
                url = new URL(urlString);
            } else {
                url = new URL(urlString + "?" + query); 
            }
            
            c = (HttpURLConnection) url.openConnection();

            c.setRequestProperty("Accept-Language", "en");
            c.setRequestProperty("Accept-Charset", "UTF-8");

            if (isPOST) {
                c.setRequestMethod("POST");
            } else {
                c.setRequestMethod("GET");
            }

            c.setDoInput(true);

            c.setDoOutput(true);

            c.setAllowUserInteraction(false);

            DataOutputStream dos = new DataOutputStream(c.getOutputStream());

            if (isPOST) {
                dos.writeBytes(query);
            }
            
            dos.close();

            c.connect();

            responseCode = (Integer.valueOf(c.getResponseCode()));

            if (getResponseCode().intValue() != 200) {
                in = c.getErrorStream();
            } else {
                in = c.getInputStream();
            }

            //readStream(in);

        } catch (MalformedURLException ex) {

            throw ex;

        } catch (IOException ex) {

            throw ex;

        }

        return in;
    }

    private String getQuery() {

        StringBuffer query = new StringBuffer();

        // add any request parameters
        Iterator<String> iter = parameterMap.keySet().iterator();

        while (iter.hasNext()) {

            String key = iter.next();

            String value = URLEncoder.encode(parameterMap.get(key));

            query.append(key).append("=").append(value).append("&");
        }

        return (query.lastIndexOf("&") > 0) ? query.substring(0, query.lastIndexOf("&")) : query.toString();
    }

    public void close() throws IOException {

        if (c != null) {
            c.disconnect();
        }
        if (cSSL != null) {
            cSSL.disconnect();
        }
        if (in != null) {
            in.close();
        }

    }

    public Integer getResponseCode() {

        return responseCode;
    }

    /**
     * @param connectTimeoutInMillis the connectTimeoutInMillis to set
     */
    public void setConnectTimeoutInMillis(int connectTimeoutInMillis) {
        this.connectTimeoutInMillis = connectTimeoutInMillis;
    }

    /**
     * @param readTimeoutInMillis the readTimeoutInMillis to set
     */
    public void setReadTimeoutInMillis(int readTimeoutInMillis) {
        this.readTimeoutInMillis = readTimeoutInMillis;
    }

    public String getURL() {
        return urlString;
    }

    public String getResponseMessage() {
        return responseMessage;
    }
}
