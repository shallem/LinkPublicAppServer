/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ws;

import com.mobilehelix.services.interfaces.WSResponse;
import com.mobilehelix.services.objects.GenericBsonResponse;
import com.mobilehelix.services.objects.WSAdminCommand;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 *
 * @author shallem
 */
@Stateless
@Path("/admin")
@PermitAll
public class AdminCommandWS {
    private static final Logger LOG = Logger.getLogger(AdminCommandWS.class.getName());
    
    @POST
    public byte[] runCmd(byte [] b) {
        int statusCode = WSResponse.FAILURE;
        String msg = "";
        try {
            WSAdminCommand adminCmd = WSAdminCommand.fromBson(b);
            
            switch(adminCmd.getCommandName()) {
                case "upgrade":
                    // Run the upgrade.
                    break;
                default:
                    break;
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to de-serialize admin command.", ex);
            statusCode = WSResponse.FAILURE;
            msg = (ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : ex.getMessage());
        }
        
        GenericBsonResponse gbr = new GenericBsonResponse(statusCode, msg);
        try {
            return gbr.toBson();
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "Failed to serialize delete app response.", ioe);
        }
        return null;
    }
}
