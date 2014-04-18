/*
 * Copyright 2013 Mobile Helix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.command.UpgradeCommand;
import com.mobilehelix.services.interfaces.WSResponse;
import com.mobilehelix.services.objects.GenericBsonResponse;
import com.mobilehelix.services.objects.WSAdminCommand;
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
@Path("/admin")
@PermitAll
public class AdminCommandWS {
    private static final Logger LOG = Logger.getLogger(AdminCommandWS.class.getName());
    
    @EJB
    private UpgradeCommand upgradeCmd;
    
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
