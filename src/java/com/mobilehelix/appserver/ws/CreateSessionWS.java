/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.push.PushManager;
import com.mobilehelix.appserver.session.SessionManager;
import com.mobilehelix.appserver.system.InitApplicationServer;
import com.mobilehelix.services.objects.CreateSessionRequest;
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
    private PushManager pushMgr;
    
    @EJB
    private InitApplicationServer initEJB;
    
    @POST
    public byte[] createSession(byte [] b) {
        int statusCode = WSResponse.FAILURE;
        String msg = null;

        CreateSessionRequest creq; 
        try {
            creq = CreateSessionRequest.fromBson(b);
            String reqSessionID = new String(creq.getServerSessionID());
            if (!initEJB.getSessID().equals(reqSessionID)) {
                /* Cannot authenticate this request. */
                statusCode = WSResponse.FAILURE;
                msg = "Failed to authentication request.";
            } else {
                sessionMgr.addSession(creq);

                statusCode = WSResponse.SUCCESS;
                msg = "Success";
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
