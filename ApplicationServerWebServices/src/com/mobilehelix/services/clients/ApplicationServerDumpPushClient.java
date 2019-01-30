package com.mobilehelix.services.clients;

import com.mobilehelix.services.interfaces.ApacheClientInterface;
import com.mobilehelix.services.interfaces.ApacheRestClient;
import com.mobilehelix.services.objects.GenericBsonResponse;
import java.io.IOException;

/**
 *
 * @author shallem
 */
public class ApplicationServerDumpPushClient extends ApacheClientInterface {
    
    public ApplicationServerDumpPushClient(String asIP,
            Integer asPort,
            ApacheRestClient cli,
            String op) {
        super(cli, asIP + ":" + asPort.toString(), "/ws/dumppush/" + op, 3);
    }
    
    public byte[] getPushSessions() throws IOException {
        byte[] output = this.getClient().bsonGet(this.getURL(), this.getNtries());
        if (output == null) {
            return null;
        }
        
        return output;
    }
    
    public GenericBsonResponse restorePushSessions(byte[] toRestore) throws IOException {
        byte[] output = this.getClient().bsonPost(this.getURL(), toRestore, this.getNtries());
        if (output == null) {
            return null;
        }
        
        return new GenericBsonResponse(output);
    }
}
