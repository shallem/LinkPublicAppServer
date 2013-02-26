/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.session.SessionManager;
import com.mobilehelix.appserver.system.InitApplicationServer;
import com.mobilehelix.wsclient.ApplicationServers.ApplicationServerSweepRequest;
import com.mobilehelix.wsclient.common.GenericBsonResponse;
import com.mobilehelix.wsclient.common.WSResponse;
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
@Path("/sweep")
@PermitAll
public class SweepSessionWS {
    private static final Logger LOGGER = Logger
        .getLogger(SweepSessionWS.class.getName());
    
    @EJB
    private SessionManager sessionMgr;
    
    @EJB
    private InitApplicationServer initEJB;
    
    @POST
    public byte[] sweepSession(byte [] b) {
        int statusCode = WSResponse.FAILURE;
        String msg = null;

        ApplicationServerSweepRequest sreq = null; 
        try {
            sreq = ApplicationServerSweepRequest.fromBson(b);
            String reqSessionID = new String(sreq.getServerSessId());
            if (!initEJB.getSessID().equals(reqSessionID)) {
                /* Cannot authenticate this request. */
                statusCode = WSResponse.FAILURE;
                msg = "Failed to authentication request.";
            } else {
                for (byte[] toDelete : sreq.getSessionKeysToDelete()) {
                    sessionMgr.deleteSession(toDelete);
                }
                
                statusCode = WSResponse.SUCCESS;
                msg = "Success";
            }
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "Sweep session failed with exception.", e);
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
            LOGGER.log(Level.SEVERE, "Failed to serialize sweep session response.", ioe);
        }
        return null;
    }
}
