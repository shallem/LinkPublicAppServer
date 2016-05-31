package com.mobilehelix.services.clients;

import com.mobilehelix.services.interfaces.RestClient;
import com.mobilehelix.services.objects.ApplicationServerDumpPushRequest;
import com.mobilehelix.services.objects.ApplicationServerPushDump;
import com.mobilehelix.services.objects.GenericBsonResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 *
 * @author shallem
 */
public class ApplicationServerDumpPushClient extends RestClient {
    
    public ApplicationServerDumpPushClient(String asIP,
            Integer asPort,
            Properties props,
            String op) {
        super(asIP + ":" + asPort.toString(), "/ws/dumppush/" + op, props);
    }
    
    public ApplicationServerPushDump getPushSessions(String sessID) throws IOException {
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
