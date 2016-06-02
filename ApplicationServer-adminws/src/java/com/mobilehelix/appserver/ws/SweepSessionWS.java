package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.session.SessionManager;
import com.mobilehelix.appserver.system.InitApplicationServer;
import com.mobilehelix.services.interfaces.WSResponse;
import com.mobilehelix.services.objects.ApplicationServerSweepRequest;
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
            if (!initEJB.validateSessionID(reqSessionID)) {
                /* Cannot authenticate this request. */
                statusCode = WSResponse.FAILURE;
                msg = "Failed to authenticate request.";
            } else {
                int expired = sreq.getSessionKeysToDelete().size();
                int deleted = 0;
                
                for (byte[] toDelete : sreq.getSessionKeysToDelete()) {
                    if (this.sessionMgr.deleteSession(toDelete)) {
                        deleted++;
                    }
                }
                
                if (deleted == expired) {
                    statusCode = WSResponse.SUCCESS;
                    msg = "Success";
                } else {
                    statusCode = WSResponse.PARTIAL_SUCCESS;
                    msg = String.valueOf(deleted) + " sessions swept out of " +
                            String.valueOf(expired);
                }
            }
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "Sweep sessions failed with exception.", e);
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
