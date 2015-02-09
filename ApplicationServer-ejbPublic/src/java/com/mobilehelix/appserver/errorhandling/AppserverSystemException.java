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
    
    /*
     * Reference to resource bundle.
     */
    private static ResourceBundle commonResources = null;
    
    /**
     * Mapping from app types to resource bundles.
     */
    private static TreeMap<Integer, ResourceBundle> appResources;
    
    private static void init() {
        commonResources = ResourceBundle.getBundle("com.mobilehelix.resources/ErrorMessages");
    }
    
    public static void registerErrorsBundle(int appType, ResourceBundle resources) {
        if (AppserverSystemException.appResources == null) {
            AppserverSystemException.appResources = new TreeMap<>();
        }
        AppserverSystemException.appResources.put(appType, resources);
    }
    
    private ResourceBundle exceptionResources;
    
    public AppserverSystemException(Exception e, String s, String key) {
        super(s);
        this.initLists(1);
        this.initCause(e);
        this.msgResourceKeys.add(key);
        this.msgResourceArgs.add(null);
        this.exceptionResources = commonResources;
    }

    public AppserverSystemException(Exception e, String s, String key, Object[] args) {
        super(s);
        this.initCause(e);
        this.initLists(1);
        this.msgResourceKeys.add(key);
        this.msgResourceArgs.add(args);
        this.exceptionResources = commonResources;
    }
    
    public AppserverSystemException(Exception e, String s, String key, Object[] args,
            int errorCode) {
        super(s);
        this.initCause(e);
        this.initLists(1);
        this.msgResourceKeys.add(key);
        this.msgResourceArgs.add(args);
        this.exceptionResources = commonResources;
        this.errorCode = errorCode;
    }
    
    public AppserverSystemException(String s, String key) {
        super(s);
        this.initLists(1);
        this.msgResourceKeys.add(key);
        this.msgResourceArgs.add(null);
        this.exceptionResources = commonResources;
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
        this.exceptionResources = commonResources;
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
        this.exceptionResources = commonResources;
    }
    
    private void initLists(int sz) {
        this.msgResourceKeys = new ArrayList<>(sz);
        this.msgResourceArgs = new ArrayList<>(sz);
    }
    
    private void initResourceBundle(int appType) {
        if (AppserverSystemException.appResources == null) {
            this.exceptionResources = null;
        }
        this.exceptionResources = AppserverSystemException.appResources.get(appType);
    }
    
    public String getMsgResourceKey() {
        return msgResourceKeys.get(0);
    }

    public Object[] getMsgResourceArgs() {
        return msgResourceArgs.get(0);
    }
    
    public String getMsgResourceKey(int idx) {
        return msgResourceKeys.get(idx);
    }
    
    public Object[] getMsgResourceArgs(int idx) {
        return msgResourceArgs.get(idx);
    } 
    
    public int getMsgResourceCount() {
        return msgResourceKeys.size();
    }

    public Integer getErrorCode() {
        return errorCode;
    }
    
    @Override
    public String getLocalizedMessage() {
        if (this.exceptionResources == null) {
            AppserverSystemException.init();
        }
        
        Object[] curArgs = this.getMsgResourceArgs();
        if (curArgs != null && this.exceptionResources != null) {
            MessageFormat mf = new MessageFormat(this.exceptionResources.getString(getMsgResourceKey()));
            String errMsg = mf.format(curArgs);
            return errMsg;
        }
        if (this.exceptionResources != null) {
            return this.exceptionResources.getString(getMsgResourceKey());
        } else {
            return this.getMsgResourceKey();
        }
    }
}
