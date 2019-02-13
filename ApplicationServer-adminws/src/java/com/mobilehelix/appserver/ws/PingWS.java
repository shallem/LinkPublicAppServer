/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.push.PushManager;
import com.mobilehelix.appserver.session.SessionManager;
import com.mobilehelix.appserver.system.ApplicationServerRegistry;
import com.mobilehelix.appserver.system.InitApplicationServer;
import com.mobilehelix.services.interfaces.WSResponse;
import com.mobilehelix.services.objects.PingRequest;
import com.mobilehelix.services.objects.PingResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author shallem
 */
@Path("/ping")
@RolesAllowed({"ApplicationServerUser", "Superuser"})
public class PingWS {
    private static final Logger LOG = Logger.getLogger(PingWS.class.getName());
    
    @Inject
    private InitApplicationServer initEJB;
    
    @Inject
    private SessionManager sessMgr;

    @Inject
    private PushManager pushMgr;
    
    @Inject
    private ApplicationServerRegistry appsRegistry;
    
    @POST
    public byte[] handlePing(InputStream is) {
        int statusCode = WSResponse.FAILURE;
        String msg = null;
        Long serverID = (long)-1;
        
        try {
            byte[] b = org.apache.commons.io.IOUtils.toByteArray(is);
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
            } else if (preq.getClient() == null) {
                statusCode = WSResponse.FAILURE;
                msg = "Must supply a client name to do a ping.";
            } else {
                // Ping all apps.
                LinkedList<String> warningMsgs = new LinkedList<>();
                boolean didSucceed = this.appsRegistry.pingAllApplications(warningMsgs, preq.getClient());
                if (!didSucceed) {
                    statusCode = WSResponse.NON_FATAL_ERROR_CODE;
                    msg = StringUtils.join(warningMsgs, ", ");
                } else {
                    statusCode = WSResponse.SUCCESS;
                    msg = "Success";
                }
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
            Long id = initEJB.getServerID();
            
            if (id == null) {
                 LOG.log(Level.SEVERE, "Server not registered. Server ID is null.");
            } else {
                resp.addServer(id, sessMgr.getSessionCount());
            }
            
            id = initEJB.getPushServerID();
            
            if (id == null) {
                 LOG.log(Level.SEVERE, "Push server not registered. Server ID is null.");
            } else {            
                resp.addServer(id, pushMgr.getPushSessionCount());
            }
        }
                
        try {
            return resp.toBson();
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "Failed to serialize ping response.", ioe);
        }
        return null;
    }
}
