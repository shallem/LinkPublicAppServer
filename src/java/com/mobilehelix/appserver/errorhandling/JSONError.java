/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.errorhandling;

import org.codehaus.jackson.io.JsonStringEncoder;

/**
 *
 * @author shallem
 */
public class JSONError {
    private String errorMessage;
    
    public JSONError(AppserverSystemException ase) {
        this.errorMessage = ase.getLocalizedMessage();
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String toJSON() {
       String errJson = "{ \"__mh_error\" : \"" + new String(JsonStringEncoder.getInstance().quoteAsString(errorMessage)) + "\" }"; 
       return errJson;
    }
}
