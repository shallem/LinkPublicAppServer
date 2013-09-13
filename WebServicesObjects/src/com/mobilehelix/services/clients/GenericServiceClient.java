/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.clients;

import com.mobilehelix.services.interfaces.RestClient;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import java.io.IOException;

/**
 *
 * @author shallem
 */
public class GenericServiceClient extends RestClient {
    public GenericServiceClient(String host, Integer port, String servicePath, HTTPSProperties props) {
        super(host + ":" + port, servicePath, props);
    }
    
    public GenericServiceClient(String host, String servicePath, HTTPSProperties props) {
        super(host, servicePath, props);
    }
    
    @Override
    public byte[] runPost(byte[] input) throws UniformInterfaceException, IOException {
        return super.runPost(input);
    }
}
