/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.errorhandling;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 *
 * @author shallem
 */
public class AppserverSystemException extends Exception {
    
    private ArrayList<String> msgResourceKeys;
    private ArrayList<Object[]> msgResourceArgs;
    
    /*
     * Reference to resource bundle.
     */
    private static ResourceBundle resources = null;
    
    private static void init() {
        resources = ResourceBundle.getBundle("com.mobilehelix.appserver.resources/ErrorMessages");
    }
    
    public AppserverSystemException(Exception e, String s, String key) {
        super(s);
        this.initLists(1);
        this.initCause(e);
        this.msgResourceKeys.add(key);
        this.msgResourceArgs.add(null);
    }

    public AppserverSystemException(Exception e, String s, String key, Object[] args) {
        super(s);
        this.initCause(e);
        this.initLists(1);
        this.msgResourceKeys.add(key);
        this.msgResourceArgs.add(args);
    }
    
    public AppserverSystemException(String s, String key) {
        super(s);
        this.initLists(1);
        this.msgResourceKeys.add(key);
        this.msgResourceArgs.add(null);
    }
    
    public AppserverSystemException(String s, String key, Object[] args) {
        super(s);
        this.initLists(1);
        this.msgResourceKeys.add(key);
        this.msgResourceArgs.add(args);
    }
    
    public AppserverSystemException(String s, List<String> keys, List<Object[]> args) {
        super(s);
        this.initLists(keys.size());
        this.msgResourceKeys.addAll(keys);
        this.msgResourceArgs.addAll(args);
    }
    
    private void initLists(int sz) {
        this.msgResourceKeys = new ArrayList<String>(sz);
        this.msgResourceArgs = new ArrayList<Object[]>(sz);
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
    
    @Override
    public String getLocalizedMessage() {
        if (resources == null) {
            AppserverSystemException.init();
        }
        
        Object[] curArgs = this.getMsgResourceArgs();
        if (curArgs != null) {
            MessageFormat mf = new MessageFormat(resources.getString(getMsgResourceKey()));
            String errMsg = mf.format(curArgs);
            return errMsg;
        }
        return resources.getString(getMsgResourceKey());
    }
}
