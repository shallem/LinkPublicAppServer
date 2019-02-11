/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.email.push.EmailPushReceiver;
import com.mobilehelix.appserver.email.push.TodayRefreshTask;
import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.push.PushManager;
import com.mobilehelix.appserver.push.PushReceiver;
import com.mobilehelix.appserver.system.InitApplicationServer;
import com.mobilehelix.services.interfaces.WSResponse;
import com.mobilehelix.services.objects.ApplicationServerAppExtensionRequest;
import com.mobilehelix.services.objects.GenericBsonResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author sethhallem
 */
@Path("/appext")
@RolesAllowed({"ApplicationServerUser", "Superuser"})
public class AppExtensionWS {

    private static final Logger LOG = Logger.getLogger(AppExtensionWS.class.getName()); 
    
    @Inject
    private PushManager pushMgr;
    
    @Inject
    private InitApplicationServer initEJB;
    
    @POST
    @Path("/today")
    public Response runCmd(byte[] b) {
        String msg = null;
        int statusCode = WSResponse.FAILURE;
        try {
            ApplicationServerAppExtensionRequest req = ApplicationServerAppExtensionRequest.fromBson(b);
            
            if (!initEJB.validateSessionID(new String(req.getServerSessID()))) {
                    /* Cannot authenticate this request. */
                statusCode = WSResponse.FAILURE;
                msg = "Failed to authenticate request.";
            } else if (!initEJB.isIsInitialized()) {
                statusCode = WSResponse.FAILURE;
                msg = "Cannot run an app extension on the app server because it is not initialized.";
            } else {
                // First, find the push session. Without it, we don't have any EmailFacade or Service with which
                // to query Exchange.
                PushReceiver pr = pushMgr.getReceiver(req.getClient(), req.getUserID(), req.getAppID());
                if (pr == null) {
                    msg ="No push session is available.";
                    statusCode = WSResponse.FAILURE;
                } else {
                    EmailPushReceiver epr = (EmailPushReceiver)pr;
                    TodayRefreshTask todayTask = epr.runTodayRefresh();
                    msg = this.pushMgr.addRefresh(req.getClient(), req.getUserID(), req.getAppID(), todayTask);
                    statusCode = WSResponse.SUCCESS;
                }
            }
            
            if (msg == null) {
                msg = "Success";
                statusCode = WSResponse.SUCCESS;
            }
            GenericBsonResponse gbr = new GenericBsonResponse(statusCode, msg);
            try {
                return Response.status(Status.OK).entity(gbr.toBson()).build();
            } catch (IOException ioe) {
                LOG.log(Level.SEVERE, "Failed to serialize delete app response.", ioe);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to deserialize app extension request", ex);
        } catch (AppserverSystemException ex) {
            LOG.log(Level.SEVERE, "Failed to run the Today app extension request", ex);
        } catch (NoSuchAlgorithmException ex) {
            LOG.log(Level.SEVERE, "Failed to store the results of the Today app extension request", ex);            
        }
        return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
}
