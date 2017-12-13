package com.mobilehelix.appserver.errorhandling;

import java.io.IOException;

/**
 *  This exception encapsulates the information from an AppServerSystemException
 *  into an IOException. The goal is to convey the information from an AppServerSystemException
 * without having to change the signature of many methods (that throw only IOExceptions)l
 * @author frederic
 */

public class AuthenticationException extends IOException {
    private final int code;
    private final int appType;
    private final String key;
    private final Object[] args;
    private final String resource; // JSON { "id": resourceID, (optional) "url", redirect_url,  (optional) "name" : resource_name }
    
    
    public AuthenticationException(int appType, String msg, String key, Object[] args, String resource) {
        this(appType, msg, key, args, resource, -1);
    }
     
    public AuthenticationException(int appType, String msg, String key, Object[] args, String resource, int code) {
        super(msg);
        this.args = args;
        this.appType = appType;
        this.key = key;
        this.code = code;
        this.resource = resource;
    }
    
    public Object[] getArgs() {
        return this.args;
    }
    
    public String getResource() {
        return this.resource;
    }

    public int getAppType() {
        return this.appType;
    }

    public String getKey() {
        return this.key;
    }
    
    public int getErrorCode() {
        return this.code;
    }
}
