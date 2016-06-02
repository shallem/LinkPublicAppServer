package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.push.PushManager;
import com.mobilehelix.appserver.session.SessionManager;
import com.mobilehelix.appserver.system.InitApplicationServer;
import com.mobilehelix.services.constants.ServerTypeConstants;
import com.mobilehelix.services.interfaces.WSResponse;
import com.mobilehelix.services.objects.CreateSessionRequest;
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
@Path("/createsession")
@PermitAll
public class CreateSessionWS {
    private static final Logger LOGGER = Logger
        .getLogger(CreateSessionWS.class.getName());
    
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
            
            if (!initEJB.validateSessionID(reqSessionID)) {
                    /* Cannot authenticate this request. */
                statusCode = WSResponse.FAILURE;
                msg = "Failed to authenticate request.";
            } else if (!initEJB.isIsInitialized()) {
                statusCode = WSResponse.FAILURE;
                msg = "Cannot create a session on the app server because it is not initialized.";
            } else {
                statusCode = WSResponse.SUCCESS;
                msg = "Success";
                switch(creq.getServerType()) {
                    case ServerTypeConstants.SERVER_TYPE_APPLICATION_SERVER:
                        sessionMgr.addSession(creq);
                        break;
                    case ServerTypeConstants.SERVER_TYPE_PUSH_SERVER:
                        pushMgr.addSession(creq);
                        break;
                    default:
                        statusCode = WSResponse.FAILURE;
                        msg = "Cannot create sessions for this server type.";
                        break;
                }
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
