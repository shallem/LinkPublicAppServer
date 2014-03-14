package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.system.InitApplicationServer;
import com.mobilehelix.services.objects.GenericBsonResponse;
import com.mobilehelix.services.interfaces.WSResponse;
import com.mobilehelix.services.objects.ApplicationServerInitRequest;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 *
 * @author shallem
 */
@Stateless
@Path("/initas")
@PermitAll
public class InitAppserverWS {
    private static final Logger LOGGER = Logger
        .getLogger(InitAppserverWS.class.getName());
        
    @Context
    private HttpServletRequest request;
    
    @EJB
    private InitApplicationServer initEJB;
    
    @POST
    public byte[] InitAS(byte [] b) {
        int statusCode = WSResponse.FAILURE;
        String msg = null;

        ApplicationServerInitRequest asir = null;
        
        try {
            asir = ApplicationServerInitRequest.fromBson(b);
            initEJB.processInitRequest(asir, request.getLocalAddr());
        
            statusCode = WSResponse.SUCCESS;
            msg = "Success";
        } catch(Exception e) {
                LOGGER.log(Level.SEVERE, "App server init failed with exception.", e);
            msg = e.getLocalizedMessage();
            statusCode = WSResponse.FAILURE;
            
            if (msg == null) {
                msg = "Unspecified failure ("+e.getClass().getSimpleName()+")";
            }
        }
        
        GenericBsonResponse gbr = new GenericBsonResponse(statusCode, msg);
        try {
            return gbr.toBson();
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Failed to serialize gateway cert response.", ioe);
        }
        return null;
    }
}
