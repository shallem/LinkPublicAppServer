/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.push.PushManager;
import com.mobilehelix.appserver.session.SessionManager;
import com.mobilehelix.appserver.system.InitApplicationServer;
import com.mobilehelix.services.interfaces.WSResponse;
import com.mobilehelix.services.objects.PingRequest;
import com.mobilehelix.services.objects.PingResponse;
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
@Path("/ping")
@PermitAll
public class PingWS {
    private static final Logger LOG = Logger.getLogger(PingWS.class.getName());
    
    @EJB
    private InitApplicationServer initEJB;
    
    @EJB
    private SessionManager sessMgr;

    @EJB
    private PushManager pushMgr;
    
    @POST
    public byte[] handlePing(byte [] b) {
        int statusCode = WSResponse.FAILURE;
        String msg = null;
        Long serverID = (long)-1;
        
        try {
            PingRequest preq = PingRequest.fromBson(b);
            String reqSessionID = new String(preq.getServerSessId());
            if (!initEJB.validateSessionID(reqSessionID)) {
                /* Cannot authenticate this request. */
                statusCode = WSResponse.FAILURE;
                msg = "Failed to authentication request.";
            } else if (initEJB.getServerID() == null) {
                /* The server is starting up and has not yet registered. */
                statusCode = WSResponse.FAILURE;
                msg = "The app server has not been registered.";
            } else {
                statusCode = WSResponse.SUCCESS;
                msg = "Success";
            }
        } catch(Exception e) {
            LOG.log(Level.SEVERE, "Ping request failed with exception.", e);
            msg = e.getLocalizedMessage();
            statusCode = WSResponse.FAILURE;
            if (msg == null) {
                msg = "Unknown failure.";
            }
        }
        
        PingResponse resp = new PingResponse(statusCode, msg);
        if (statusCode == WSResponse.SUCCESS) {
            // Add the result for the main app server.
            resp.addServer(initEJB.getServerID(), sessMgr.getSessionCount());
            resp.addServer(initEJB.getPushServerID(), pushMgr.getPushSessionCount());
        }
                
        try {
            return resp.toBson();
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "Failed to serialize ping response.", ioe);
        }
        return null;
    }
}
