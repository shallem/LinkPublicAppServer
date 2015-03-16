/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.errorhandling;

/**
 *
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
