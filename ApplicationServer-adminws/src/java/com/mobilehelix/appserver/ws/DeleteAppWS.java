/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.system.ApplicationServerRegistry;
import com.mobilehelix.appserver.system.InitApplicationServer;
import com.mobilehelix.services.interfaces.WSResponse;
import com.mobilehelix.services.objects.ApplicationServerAppDeleteRequest;
import com.mobilehelix.services.objects.GenericBsonResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 *
 * @author shallem
 */
@Stateless
@Path("/deleteapp")
@PermitAll
public class DeleteAppWS {
    private static final Logger LOGGER = Logger
        .getLogger(DeleteAppWS.class.getName());
    
    @EJB
    private ApplicationServerRegistry appRegistry;
    
    @EJB
    private InitApplicationServer initEJB;
    
    @POST
    public byte[] deleteApp(byte [] b) {
        int statusCode = WSResponse.FAILURE;
        String msg = null;

        ApplicationServerAppDeleteRequest sreq = null; 
        try {
            sreq = ApplicationServerAppDeleteRequest.fromBson(b);
            String reqSessionID = new String(sreq.getServerSessId());
            if (!initEJB.validateSessionID(reqSessionID)) {
                /* Cannot authenticate this request. */
                statusCode = WSResponse.FAILURE;
                msg = "Failed to authentication request.";
            } else {
                Long toDelete = sreq.getAppID();
                appRegistry.deleteAppFromRegistry(sreq.getClient(), toDelete);
                
                statusCode = WSResponse.SUCCESS;
                msg = "Success";
            }
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "Delete app failed with exception.", e);
            msg = e.getLocalizedMessage();
            statusCode = WSResponse.FAILURE;
            if (msg == null) {
                msg = "Unknown failure.";
            }
        }
        
        GenericBsonResponse gbr = new GenericBsonResponse(statusCode, msg);
        try {
            return gbr.toBson();
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Failed to serialize delete app response.", ioe);
        }
        return null;
    }
}
