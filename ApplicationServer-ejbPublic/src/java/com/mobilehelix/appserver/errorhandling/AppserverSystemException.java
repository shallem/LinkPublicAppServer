/*
 * Copyright 2013 Mobile Helix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mobilehelix.appserver.errorhandling;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeMap;

/**
 *
 * @author shallem
 */
public class AppserverSystemException extends Exception {
    
    private ArrayList<String> msgResourceKeys;
    private ArrayList<Object[]> msgResourceArgs;
    private Integer errorCode;
    private String resource;  // JSON { "id": resourceID, (optional) "url", redirect_url,  (optional) "name" : resource_name }
    
    /*
     * Reference to resource bundle.
     */
    private static ResourceBundle commonResources;
    
    /**
     * Mapping from app types to resource bundles.
     */
    private static TreeMap<Integer, ResourceBundle> appResources = new TreeMap<>();
        
    private static ResourceBundle getResourceBundle() {
        if (commonResources == null) {
           commonResources = ResourceBundle.getBundle("com.mobilehelix.resources/ErrorMessages");
        }
        
        return commonResources;
    }
    
    public static void registerErrorsBundle(int appType, ResourceBundle resources) {
        appResources.put(appType, resources);
    }
    
    private ResourceBundle exceptionResources;
    
    public AppserverSystemException(Exception e, String s, String key) {
        super(s);
        this.initLists(1);
        this.initCause(e);
        this.msgResourceKeys.add(key);
        this.msgResourceArgs.add(null);
        this.exceptionResources = getResourceBundle();
    }

    public AppserverSystemException(Exception e, String s, String key, Object[] args) {
        super(s);
        this.initCause(e);
        this.initLists(1);
        this.msgResourceKeys.add(key);
        this.msgResourceArgs.add(args);
        this.exceptionResources = getResourceBundle();
    }
    
    public AppserverSystemException(Exception e, String s, String key, Object[] args,
            int errorCode) {
        super(s);
        this.initCause(e);
        this.initLists(1);
        this.msgResourceKeys.add(key);
        this.msgResourceArgs.add(args);
        this.exceptionResources = getResourceBundle();
        this.errorCode = errorCode;
    }
    
    public AppserverSystemException(String s, String key) {
        super(s);
        this.initLists(1);
        this.msgResourceKeys.add(key);
        this.msgResourceArgs.add(null);
        this.exceptionResources = getResourceBundle();
    }
    
    public AppserverSystemException(int appType, String s, String key) {
        super(s);
        this.initLists(1);
        this.msgResourceKeys.add(key);
        this.msgResourceArgs.add(null);
        this.initResourceBundle(appType);
    }
    
    public AppserverSystemException(int appType, String s, String key,
            int errorCode) {
        super(s);
        this.initLists(1);
        this.msgResourceKeys.add(key);
        this.msgResourceArgs.add(null);
        this.initResourceBundle(appType);
        this.errorCode = errorCode;
    }
    
    public AppserverSystemException(String s, String key, Object[] args) {
        super(s);
        this.initLists(1);
        this.msgResourceKeys.add(key);
        this.msgResourceArgs.add(args);
        this.exceptionResources = getResourceBundle();
    }
    
    public AppserverSystemException(int appType, String s, String key, Object[] args) {
        super(s);
        this.initLists(1);
        this.msgResourceKeys.add(key);
        this.msgResourceArgs.add(args);
        this.initResourceBundle(appType);
    }
    
    public AppserverSystemException(int appType, String s, String key, Object[] args,
            int errorCode) {
        super(s);
        this.initLists(1);
        this.msgResourceKeys.add(key);
        this.msgResourceArgs.add(args);
        this.initResourceBundle(appType);
        this.errorCode = errorCode;
    }
    
    public AppserverSystemException(String s, List<String> keys, List<Object[]> args) {
        super(s);
        this.initLists(keys.size());
        this.msgResourceKeys.addAll(keys);
        this.msgResourceArgs.addAll(args);
        this.exceptionResources = getResourceBundle();
    }
    
    private void initLists(int sz) {   
        this.msgResourceKeys = new ArrayList<>(sz);
        this.msgResourceArgs = new ArrayList<>(sz);
    }
    
    private void initResourceBundle(int appType) {
        if (appResources == null) {
            this.exceptionResources = null;
        }
        this.exceptionResources = appResources.get(appType);
    }
    
    public String getMsgResourceKey() {
        return this.getMsgResourceKey(0);
    }

    public Object[] getMsgResourceArgs() {
        return this.getMsgResourceArgs(0);
    }
    
    public String getMsgResourceKey(int idx) {
        return (idx < this.msgResourceKeys.size()) ? this.msgResourceKeys.get(idx) : "" ;
    }
    
    public Object[] getMsgResourceArgs(int idx) {
        return (idx < this.msgResourceArgs.size()) ? this.msgResourceArgs.get(idx) : new Object[0];
    } 
    
    public int getMsgResourceCount() {
        return msgResourceKeys.size();
    }

    public int getErrorCode() {
        return (this.errorCode == null) ? -1 : this.errorCode;
    }
    
    @Override
    public String getLocalizedMessage() {
        Object[] curArgs = this.getMsgResourceArgs();
        if (curArgs != null && this.exceptionResources != null) {
            MessageFormat mf = new MessageFormat(this.exceptionResources.getString(getMsgResourceKey()));
            return mf.format(curArgs);
        }
        if (this.exceptionResources != null) {
            return this.exceptionResources.getString(this.getMsgResourceKey());
        } 
            
        return this.getMsgResourceKey();
    }
    
    @Override
    public String getMessage() {
        return this.getLocalizedMessage();
    }

     // JSON object { "id" : xxx, "url" : yyy } as String
    public String getResource() {
        return this.resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }       
}
