/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.methods;

import java.net.URI;
import org.apache.http.client.methods.HttpRequestBase;

/**
 *
 * @author v3devel
 */
public class HttpMove extends HttpRequestBase {

    public HttpMove(URI uri) {
        super();
        this.setURI(uri);
    }
    
    @Override
    public String getMethod() {
        return "MOVE";
    }
}
