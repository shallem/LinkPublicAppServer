/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.push.PushManager;
import com.mobilehelix.appserver.push.PushRefresh;
import com.mobilehelix.services.interfaces.WSResponse;
import com.mobilehelix.services.objects.ApplicationServerRefreshResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author v3devel
 */
@Path("/pushrefresh")
@RolesAllowed({"ApplicationServerUser", "Superuser"})
public class PushRefreshWS {
    private static final Logger LOG = Logger.getLogger(PushRefreshWS.class.getName());
    
    @Inject
    private PushManager pushMgr;
    
    @GET
    @Path("/refresh")
    @Produces("application/octet-stream")
    public Response doPushRefresh(@Context UriInfo uriInfo) {
        MultivaluedMap<String, String> form = uriInfo.getQueryParameters();
        try {
            PushRefresh action = pushMgr.executeRefreshAction(form);
            if (action == null) {
                // Stale request - we only queue up one push action of each type per user account.
                return Response.noContent().build();                                
            }
            
            byte[] refreshData = action.getRefresh(form);
            if (refreshData == null) {
                LOG.log(Level.INFO, "Background push refresh failed with error - the refresh data is null. This can be caused by a bg refresh from a device that has not logged in for a long time. Also, check logs for a possible exception in runRefresh.");
                return Response.noContent().build();                
            }
            ApplicationServerRefreshResponse resp = new ApplicationServerRefreshResponse(WSResponse.SUCCESS, "Success", refreshData);
            return Response.ok(resp.toBson()).build();
        } catch(AppserverSystemException ex) {
            LOG.log(Level.INFO, "Background push refresh failed with error", ex);
            ApplicationServerRefreshResponse resp = new ApplicationServerRefreshResponse(WSResponse.FAILURE, ex.getLocalizedMessage());
            try {
                return Response.ok(resp.toBson()).build();
            } catch (IOException ex1) {
                return Response.ok(WSResponse.FAILURE_STRING).build();
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to serialize refresh response", ex);
            return Response.serverError().build();
        }
    }
}
