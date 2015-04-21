package com.mobilehelix.services.clients;

import com.mobilehelix.services.interfaces.RestClient;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import java.io.IOException;
import java.util.TreeMap;
import org.apache.commons.codec.EncoderException;
import org.helix.mobile.model.ClientWSResponse;

/**
 *
 * @author shallem
 */
public class ApplicationServerFilesClient extends RestClient {
    
    public ApplicationServerFilesClient(String host,
            HTTPSProperties props,
            String op) {
        super(host, "/clientws/files/" + op, props);
    }
    
    public ClientWSResponse getRoots(String sessID, Long appID) throws UniformInterfaceException, IOException, EncoderException {
        TreeMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("sessionid", sessID);
        paramsMap.put("appid", appID.toString());
        super.appendQueryParameters(paramsMap);
        
        return super.runJSONGet(ClientWSResponse.class);
    }
}
