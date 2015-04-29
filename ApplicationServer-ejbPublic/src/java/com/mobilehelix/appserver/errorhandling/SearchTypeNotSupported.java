package com.mobilehelix.appserver.errorhandling;

/**
 * Extends AppserverSystemException to convey the information that the
 * underlying file system does not support the type of search provided in the
 * request. The goal is to make it easy to manage this kind of error separately from
 * actual query failures
 * @author Seth
 */
public class SearchTypeNotSupported extends AppserverSystemException {

    public SearchTypeNotSupported(int appType, String s, String key) {
        super(appType, s, key);
    }
    
    public SearchTypeNotSupported(Exception e, String s, String key) {
        super(e, s, key);
    }
    
}
