/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.system.InitApplicationServer;
import com.mobilehelix.wsclient.common.GenericBsonResponse;
import com.mobilehelix.wsclient.common.ServerPingRequest;
import com.mobilehelix.wsclient.common.WSResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class PingWS {
    private static final Logger LOG = Logger.getLogger(PingWS.class.getName());
    
    @EJB
    private InitApplicationServer initEJB;
    
    @POST
    public byte[] handlePing(byte [] b) {
        int statusCode = WSResponse.FAILURE;
        String msg = null;
        
        try {
            ServerPingRequest preq = ServerPingRequest.fromBson(b);
            String reqSessionID = new String(preq.getServerSessId());
            if (!initEJB.getSessID().equals(reqSessionID)) {
                /* Cannot authenticate this request. */
                statusCode = WSResponse.FAILURE;
                msg = "Failed to authentication request.";
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
        
        GenericBsonResponse gbr = new GenericBsonResponse(statusCode, msg);
        try {
            return gbr.toBson();
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "Failed to serialize ping response.", ioe);
        }
        return null;
    }
}
