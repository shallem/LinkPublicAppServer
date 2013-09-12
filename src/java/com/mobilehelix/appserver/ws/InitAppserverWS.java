/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.system.InitApplicationServer;
import com.mobilehelix.wsclient.ApplicationServers.ApplicationServerInitRequest;
import com.mobilehelix.services.objects.GenericBsonResponse;
import com.mobilehelix.services.interfaces.WSResponse;
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
            initEJB.init(asir.getControllerIP(), asir.getControllerPort(), 
                    asir.getAsPubIP(), request.getLocalAddr(), asir.getAsPubPort(), asir.getAsPrivPort(),
                    asir.getClientName(), asir.getServerName(), asir.getPushServerName(),
                    asir.getStorePass(), asir.getKeyStore(),
                    asir.getRegionName());
        
            statusCode = WSResponse.SUCCESS;
            msg = "Success";
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "App server init failed with exception.", e);
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
            LOGGER.log(Level.SEVERE, "Failed to serialize gateway cert response.", ioe);
        }
        return null;
    }
}
