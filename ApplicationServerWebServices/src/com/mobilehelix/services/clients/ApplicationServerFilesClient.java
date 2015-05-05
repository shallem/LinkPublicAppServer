package com.mobilehelix.services.clients;

import com.mobilehelix.services.interfaces.RestClient;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import java.io.IOException;
import java.util.TreeMap;
import org.apache.commons.codec.EncoderException;

/**
 *
 * @author shallem
 */
public class ApplicationServerFilesClient extends RestClient {
    
    public ApplicationServerFilesClient(String host,
            HTTPSProperties props,
            String op) {
        super(host, "/clientws/files/" + op, props);
    }
    
    public String getRoots(String sessID, Long appID) throws UniformInterfaceException, IOException, EncoderException {
        TreeMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("sessionid", sessID);
        paramsMap.put("appid", appID.toString());
        super.appendQueryParameters(paramsMap);
        
        byte[] res = super.runGet();
        if (res != null) {
            return new String(res);
        }
        return null;
    }
    
    public String syncDir(String sessID, Long appID, String rootDigest, String syncTarget, String state) throws UniformInterfaceException, IOException, EncoderException {
        TreeMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("sessionid", sessID);
        paramsMap.put("appid", appID.toString());
        if (rootDigest != null) {
            paramsMap.put("digest", rootDigest);
        } else {
            paramsMap.put("digest", "ROOT");
        }
        if (syncTarget != null) {
            paramsMap.put("target", syncTarget);
        }
        if (state != null) {
            paramsMap.put("state", state);
        }
        super.appendQueryParameters(paramsMap);
        
        byte[] res = super.runGet();
        if (res != null) {
            return new String(res);
        }
        return null;
    } 
    
    public String getFileInfo(String sessID, Long appID, String rootDigest, String fileID) throws UniformInterfaceException, IOException, EncoderException {
        TreeMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("sessionid", sessID);
        paramsMap.put("appid", appID.toString());
        if (rootDigest != null) {
            paramsMap.put("digest", rootDigest);
        } else {
            paramsMap.put("digest", "ROOT");
        }
        paramsMap.put("id", fileID);
        super.appendQueryParameters(paramsMap);
        
        byte[] res = super.runGet();
        if (res != null) {
            return new String(res);
        }
        return null;
    }  
    
    public byte[] downloadFile(String sessID, Long appID, String rootDigest, String fileID, String fileName) throws UniformInterfaceException, IOException, EncoderException {
        TreeMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("sessionid", sessID);
        paramsMap.put("appid", appID.toString());
        if (rootDigest != null) {
            paramsMap.put("digest", rootDigest);
        } else {
            paramsMap.put("digest", "ROOT");
        }
        paramsMap.put("id", fileID);
        if (fileName != null) {
            paramsMap.put("filename", fileName);
        }
        super.appendQueryParameters(paramsMap);
        
        return super.runGet();
    } 
}
