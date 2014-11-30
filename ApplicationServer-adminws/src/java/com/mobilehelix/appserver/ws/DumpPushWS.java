package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.push.PushManager;
import com.mobilehelix.appserver.push.PushReceiver;
import com.mobilehelix.appserver.system.InitApplicationServer;
import com.mobilehelix.services.interfaces.WSResponse;
import com.mobilehelix.services.objects.ApplicationServerDumpPushRequest;
import com.mobilehelix.services.objects.ApplicationServerPushDump;
import com.mobilehelix.services.objects.ApplicationServerPushSession;
import com.mobilehelix.services.objects.GenericBsonResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Dumps all active push sessions. We validate the session ID prior to accepting this call.
 * 
 * @author shallem
 */
@Stateless
@Path("/dumppush")
@PermitAll
public class DumpPushWS {
    private static final Logger LOG = Logger.getLogger(DumpPushWS.class.getName());
    
    @EJB
    private PushManager pushMgr;
    
    @EJB
    private InitApplicationServer initEJB;
    
    @POST
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    @Path("/get")
    public byte[] getPushSessions(byte[] input) {
        int statusCode;
        String msg;
        ApplicationServerPushDump ret = new ApplicationServerPushDump();
        
        try {
            ApplicationServerDumpPushRequest req = ApplicationServerDumpPushRequest.fromBson(input);
            String reqID = new String(req.getServerSessId(), Charset.defaultCharset());

            if (!initEJB.validateSessionID(reqID)) {
                    /* Cannot authenticate this request. */
                statusCode = WSResponse.FAILURE;
                msg = "Failed to authenticate request.";
            } else if (!initEJB.isIsInitialized()) {
                statusCode = WSResponse.FAILURE;
                msg = "Cannot dump push sessions on the app server because it is not initialized.";
            } else {
                statusCode = WSResponse.SUCCESS;
                msg = "Success";
                
                for (PushReceiver pr : this.pushMgr.allSessions()) {
                    ApplicationServerPushSession nxt = new ApplicationServerPushSession();
                    nxt.setUniqueID(pr.getUniqueID());
                    nxt.setClientid(pr.getClientid());
                    nxt.setUserid(pr.getUserid());
                    nxt.setPassword(pr.getPassword());
                    nxt.setDeviceType(pr.getDeviceType());
                    nxt.setAppID(pr.getAppID());
                    ret.addPushSession(nxt);
                }
            }
        } catch(IOException ioe) {
            statusCode = WSResponse.FAILURE;
            msg = ioe.getMessage();
        }
        
        ret.setStatusCode(statusCode);
        ret.setMsg(msg);
        try {
            return ret.toBson();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to serialize push dump response.", ex);
        }
        return null;
    }
    
    @POST
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    @Path("/restore")
    public byte[] restorePushSessions(byte[] input) {
        int statusCode;
        String msg;
        GenericBsonResponse ret = null;
        
        try {
            ApplicationServerPushDump req = ApplicationServerPushDump.createFromBson(input);

            // WE do not validate with the session ID here. Generally it is not necessary - b/c
            // (a) the client is already authenticated with a certificate, and (b) if the provided data
            // is full of bogus credentials then it really doesn't create any leak of sensitive information.
            // Creating such push sessions will fail when the attempting to authenticate with A-D.
            if (!initEJB.isIsInitialized()) {
                statusCode = WSResponse.FAILURE;
                msg = "Cannot restore push sessions on the app server because it is not initialized.";
            } else {
                statusCode = WSResponse.SUCCESS;
                msg = "Success";
                
                for (ApplicationServerPushSession aps : req.getPushSessions()) {
                    try {
                        this.pushMgr.addSession(aps.getClientid(), aps.getUserid(), aps.getPassword(),
                                aps.getDeviceType(), aps.getAppID(), 0);
                    } catch (AppserverSystemException ex) {
                        LOG.log(Level.SEVERE, "Unable to restore push session for {0}:{1}", new Object[] {
                            aps.getClientid(), aps.getUserid()
                        });
                        LOG.log(Level.SEVERE, "Unable to restore push session.", ex);
                    }
                }
            }
        } catch(IOException ioe) {
            statusCode = WSResponse.FAILURE;
            msg = ioe.getMessage();
        }
        
        ret = new GenericBsonResponse(statusCode, msg);
        try {
            return ret.toBson();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to serialize push dump response.", ex);
        }
        return null;
    }
}
