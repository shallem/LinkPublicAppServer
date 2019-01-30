package com.mobilehelix.services.clients;

import com.mobilehelix.services.interfaces.ApacheClientInterface;
import com.mobilehelix.services.interfaces.ApacheRestClient;
import com.mobilehelix.services.utils.RestURLUtils;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author shallem
 */
public class ApplicationServerFilesClient extends ApacheClientInterface {
    
    public ApplicationServerFilesClient(String host,
            ApacheRestClient cli,
            String op) {
        super(cli, host, "/clientws/files/" + op, 3);
    }
    
    public String getRoots(String sessID, Long appID) throws IOException {
        Properties paramsMap = new Properties();
        paramsMap.put("sessionid", sessID);
        paramsMap.put("appid", appID.toString());
        String qry = RestURLUtils.genQueryParams(paramsMap);
        
        byte[] res = this.getClient().bsonGet(this.getURL(), qry, this.getNtries());
        if (res != null) {
            return new String(res);
        }
        return null;
    }
    
    public String syncDir(String sessID, Long appID, String rootDigest, String syncTarget, String state) throws IOException {
        Properties paramsMap = new Properties();
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
        String qry = RestURLUtils.genQueryParams(paramsMap);
        
        byte[] res = this.getClient().bsonGet(this.getURL(), qry, this.getNtries());
        if (res != null) {
            return new String(res);
        }
        return null;
    } 
    
    public String getFileInfo(String sessID, Long appID, String rootDigest, String fileID) throws IOException {
        Properties paramsMap = new Properties();
        paramsMap.put("sessionid", sessID);
        paramsMap.put("appid", appID.toString());
        if (rootDigest != null) {
            paramsMap.put("digest", rootDigest);
        } else {
            paramsMap.put("digest", "ROOT");
        }
        paramsMap.put("id", fileID);
        String qry = RestURLUtils.genQueryParams(paramsMap);
        
        byte[] res = this.getClient().bsonGet(this.getURL(), qry, this.getNtries());
        if (res != null) {
            return new String(res);
        }
        return null;
    }  
    
    public byte[] downloadFile(String sessID, Long appID, String rootDigest, String fileID, String fileName) throws IOException {
        Properties paramsMap = new Properties();
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
        String qry = RestURLUtils.genQueryParams(paramsMap);
        
        return this.getClient().bsonGet(this.getURL(), qry, this.getNtries());
    } 
}
