/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.interfaces;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author shallem
 */
public abstract class RestClient {
    private Client c;
    private String webURL;
    private ClientResponse resp;
    private Integer connectTimeout;
    private Integer readTimeout;
    
    // Specified in milliseconds
    private static final int DEFAULT_CONNECT_TIMEOUT = 20000;
    private static final int DEFAULT_READ_TIMEOUT = 20000;
    
    // Properties map containing headers to add to the request.
    private Properties headersToAdd;
    
    public RestClient(String host, String path) {
        this.CreateJerseyClient(null);
        this.initWebURL("http://", host, -1, path, null);
    }
        
    public RestClient(String host, String path, HTTPSProperties props) {
        this.CreateJerseyClient(props);
        this.initWebURL("https://", host, -1, path, null);
    }
    
    public RestClient(String host, int port, String path) {
        this.CreateJerseyClient(null);
        this.initWebURL("http://", host, port, path, null);
    }
    
    public RestClient(String host, int port, String path, HTTPSProperties props) {
        this.CreateJerseyClient(props);
        this.initWebURL("https://", host, port, path, null);
    }
    
    public RestClient(String host, int port, String path, String[] pathArgs) {
        this.CreateJerseyClient(null);
        this.initWebURL("http://", host, port, path, pathArgs);
    }
    
    public RestClient(String host, int port, String path, String[] pathArgs, HTTPSProperties props) {
        this.CreateJerseyClient(props);
        this.initWebURL("https://", host, port, path, pathArgs);
    }
    
    private void initWebURL(String scheme, String host, int port, String path, String[] pathArgs) {
        System.out.println("Initializing dest URL.");
        webURL = scheme + host; 
        if (port > 0) {
            webURL = webURL + ":" + port;
        }
        webURL = webURL + path;
        if (pathArgs != null) {
            StringBuilder pathArgString = new StringBuilder();
            for (String s : pathArgs) {
                try {
                    pathArgString.append("/");
                    pathArgString.append(URLEncoder.encode(s, "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    // Should *never* happen because UTF-8 is always a supported encoding.
                    Logger.getLogger(RestClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            webURL = webURL + pathArgString.toString();
        }
        System.out.println("Connecting to: " + webURL);
    }

    protected void appendWebURLPath(String path) {
        this.webURL = this.webURL + "/" + path;
    }
    
    private void CreateJerseyClient(HTTPSProperties prop) {
        System.out.println("Creating Jersey client.");
        resp = null;
        connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        readTimeout = DEFAULT_READ_TIMEOUT;
        
        c = Client.create();
        if (prop != null) {
             c.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, prop);
        }
        
        headersToAdd = new Properties();
    }
    
    /**
     * setConnectTimeout
     * 
     * Set the timeout interval for Jersey client connections. Max value of seconds
     * is MAX_SHORT.
     * @param timeoutSecs
     */
    public void setConnectTimeout(short timeoutSecs) {
        connectTimeout = timeoutSecs * 1000;
    }
    
    /**
     * setReadTimeout
     * 
     * Set the timeout interval for Jersey client network reads. Max value of seconds
     * is MAX_SHORT.
     * @param timeoutSecs
     */
    public void setReadTimeout(short timeoutSecs) {
        readTimeout = timeoutSecs * 1000;
    }
    
    private void setTimeouts() {
        c.setConnectTimeout(connectTimeout);
        c.setReadTimeout(readTimeout);
    }
    
    private WebResource.Builder AddHeadersToResource(WebResource r) {
        WebResource.Builder b = r.getRequestBuilder();
        for (String key : headersToAdd.stringPropertyNames()) {
            b = b.header(key, headersToAdd.getProperty(key));
        }
        return b;
    }
    
    protected void AddHeader(String key, String value) {
        this.headersToAdd.setProperty(key, value);
    }
    
    private byte[] readResponseData() throws UniformInterfaceException, IOException {
        InputStream is = resp.getEntityInputStream();
        byte[] b = new byte[1024 * 32];
        int nRead;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((nRead = is.read(b, 0, b.length)) != -1) {
            baos.write(b, 0, nRead);
        }
        
        baos.flush();
        return baos.toByteArray();
    }
    
    /**
     * run
     * 
     * Runs a service request directed at the webresource specified using the host and
     * path name supplied to the constructor. The input to this method is a properties object
     * mapping strings to strings. In this case, the serialized data sent as the post body is
     * in the form of an http post form (x/www-form-urlencoded). Other versions of the run method
     * will send different types of post bodies.
     *
     * @param p key value map from which the post body is generated.
     * @return returns the serialized form of the response
     */
    protected byte[] run(String method, Form f) throws UniformInterfaceException, IOException {
        setTimeouts();
        WebResource r = c.resource(this.webURL);
        WebResource.Builder builder = this.AddHeadersToResource(r);
        resp = builder.type(MediaType.APPLICATION_FORM_URLENCODED).method(method, ClientResponse.class, f);
        if (resp.getStatus() != 200) {
            // Request failed.
            return null;
        }
        return readResponseData();
    }

    protected byte[] runPost(Form f) throws UniformInterfaceException, IOException {
        return this.run("POST", f);
    }
    
    protected byte[] runPut(Form f) throws UniformInterfaceException, IOException {
        return this.run("PUT", f);
    }
 
    protected byte[] run(String method, byte[] input) throws UniformInterfaceException, IOException {
        setTimeouts();
        WebResource r = c.resource(this.webURL);
        WebResource.Builder builder = this.AddHeadersToResource(r);
        resp = builder.type(MediaType.APPLICATION_OCTET_STREAM).method(method, ClientResponse.class, input);
        if (resp.getStatus() != 200) {
            // Request failed.
            return null;
        }
        return readResponseData();
    }
    
    protected byte[] runPut(WSRequest req) throws UniformInterfaceException, IOException {
        return this.run("PUT", req.toBson());
    }
    
    protected byte[] runPost(WSRequest req) throws UniformInterfaceException, IOException {
        return this.run("POST", req.toBson());
    }
    
    protected byte[] runPut(byte[] req) throws UniformInterfaceException, IOException {
        return this.run("PUT", req);
    }
    
    protected byte[] runPost(byte[] req) throws UniformInterfaceException, IOException {
        return this.run("POST", req);
    }
    
    protected byte[] runPost() throws UniformInterfaceException, IOException {
        return this.runNoBody("POST", this.webURL);
    }
    
    protected byte[] runNoBody(String method, String url) throws UniformInterfaceException, IOException {
        setTimeouts();
        WebResource r = c.resource(url);
        WebResource.Builder builder = this.AddHeadersToResource(r);
        resp = builder.method(method, ClientResponse.class);
        return readResponseData();
    }
    
    protected InputStream runStreamingNoBody(String method, String url) throws UniformInterfaceException, IOException {
        setTimeouts();
        WebResource r = c.resource(url);
        WebResource.Builder builder = this.AddHeadersToResource(r);
        resp = builder.method(method, ClientResponse.class);
        if (resp.getStatus() != 200) {
            return null;
        }
        return resp.getEntityInputStream();
    }
    
    protected byte[] runGet() throws UniformInterfaceException, IOException {
        return runNoBody("GET", this.webURL);
    }
    
    protected byte[] runGet(String params) throws UniformInterfaceException, IOException {
        String fullURI = webURL + "?" + params;
        return runNoBody("GET", fullURI);
    }
    
    protected InputStream runStreamingGet(String params) throws UniformInterfaceException, IOException {
        String fullURI = webURL + "?" + params;
        return runStreamingNoBody("GET", fullURI);
    }
    
    protected byte[] runDelete() throws UniformInterfaceException, IOException {
        return runNoBody("DELETE", this.webURL);
    }
    
    protected byte[] runDelete(String params) throws UniformInterfaceException, IOException {
        String fullURI = this.webURL + "?" + params;
        return runNoBody("DELETE", fullURI);
    }
}
