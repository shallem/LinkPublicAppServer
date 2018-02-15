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
    
    public byte[] getPushSessions() throws UniformInterfaceException, IOException {
        byte[] output = super.runGet();
        if (output == null) {
            return null;
        }
        
        return output;
    }
    
    public GenericBsonResponse restorePushSessions(byte[] toRestore) throws IOException {
        byte[] output = super.runPost(toRestore);
        if (output == null) {
            return null;
        }
        
        return new GenericBsonResponse(output);
    }
}
