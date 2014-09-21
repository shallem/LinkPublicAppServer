/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.clients;

import com.mobilehelix.services.interfaces.RestClient;
import com.mobilehelix.services.objects.ApplicationServerDumpPushRequest;
import com.mobilehelix.services.objects.ApplicationServerPushDump;
import com.mobilehelix.services.objects.GenericBsonResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 *
 * @author shallem
 */
public class ApplicationServerDumpPushClient extends RestClient {
    
    public ApplicationServerDumpPushClient(String asIP,
            Integer asPort,
            HTTPSProperties props,
            String op) {
        super(asIP + ":" + asPort.toString(), "/ws/dumppush/" + op, props);
    }
    
    public ApplicationServerPushDump getPushSessions(String sessID) throws UniformInterfaceException, IOException {
        ApplicationServerDumpPushRequest req = new ApplicationServerDumpPushRequest();
        req.setServerSessId(sessID.getBytes(Charset.defaultCharset()));
        
        byte[] output = super.runPost(req.toBson());
        if (output == null) {
            return null;
        }
        
        return ApplicationServerPushDump.createFromBson(output);
    }
    
    public GenericBsonResponse restorePushSessions(ApplicationServerPushDump toRestore) throws IOException {
        byte[] output = super.runPost(toRestore.toBson());
        if (output == null) {
            return null;
        }
        
        return new GenericBsonResponse(output);
    }
}
