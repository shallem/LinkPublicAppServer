package com.mobilehelix.services.clients;

import com.mobilehelix.services.interfaces.ApacheClientInterface;
import com.mobilehelix.services.interfaces.ApacheRestClient;
import com.mobilehelix.services.objects.GenericBsonResponse;
import java.io.IOException;

/**
 *
 * @author shallem
 */
public class ApplicationServerDumpSessClient extends ApacheClientInterface {
    
    public ApplicationServerDumpSessClient(String asIP,
            Integer asPort,
            ApacheRestClient cli,
            String op) {
        super(cli, asIP + ":" + asPort.toString(), "/ws/dumpsess/" + op, 3);
    }
    
    public byte[] getSessions() throws IOException {
        byte[] output = this.getClient().bsonGet(this.getURL(), this.getNtries());
        if (output == null) {
            return null;
        }
        
        return output;
    }
    
    public GenericBsonResponse restoreSessions(byte[] toRestore) throws IOException {
        byte[] output = this.getClient().bsonPost(this.getURL(), toRestore, this.getNtries());
        if (output == null) {
            return null;
        }
        
        return new GenericBsonResponse(output);
    }
}
