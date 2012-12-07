/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.session.SessionManager;
import com.mobilehelix.appserver.system.InitApplicationServer;
import com.mobilehelix.constants.ServerTypeConstants;
import com.mobilehelix.wsclient.ApplicationServers.ApplicationServerInitRequest;
import com.mobilehelix.wsclient.common.CreateSessionRequest;
import com.mobilehelix.wsclient.common.GenericBsonResponse;
import com.mobilehelix.wsclient.common.WSResponse;
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
@Path("/createsession")
@PermitAll
public class CreateSessionWS {
    private static final Logger LOGGER = Logger
        .getLogger(CreateSessionWS.class.getName());
        
    @Context
    private HttpServletRequest request;
    
    @EJB
    private SessionManager sessionMgr;
    
    @EJB
    private InitApplicationServer initEJB;
    
    @POST
    public byte[] createSession(byte [] b) {
        int statusCode = WSResponse.FAILURE;
        String msg = null;

        CreateSessionRequest creq = null; 
        try {
            creq = CreateSessionRequest.fromBson(b);
            if (creq.getServerType() == ServerTypeConstants.SERVER_TYPE_APPLICATION_SERVER) {
                String reqSessionID = new String(creq.getServerSessId());
                if (!initEJB.getSessID().equals(reqSessionID)) {
                    /* Cannot authenticate this request. */
                    statusCode = WSResponse.FAILURE;
                    msg = "Failed to authentication request.";
                } else {
                    sessionMgr.addSession(creq.getSess());

                    statusCode = WSResponse.SUCCESS;
                    msg = "Success";
                }
            } else if (creq.getServerType() == ServerTypeConstants.SERVER_TYPE_PUSH_SERVER) {
                
            }
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "Create session failed with exception.", e);
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
            LOGGER.log(Level.SEVERE, "Failed to serialize create session response.", ioe);
        }
        return null;
    }
}
