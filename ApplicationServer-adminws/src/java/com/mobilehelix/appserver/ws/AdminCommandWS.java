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
import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.system.ApplicationServerRegistry;
import com.mobilehelix.appserver.system.InitApplicationServer;
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
    
    @EJB
    private ApplicationServerRegistry appRegistry;
    
    @EJB
    private InitApplicationServer initEJB;
    
    @POST
    public byte[] runCmd(byte [] b) {
        int statusCode = WSResponse.FAILURE;
        String msg = null;
        try {
            WSAdminCommand adminCmd = WSAdminCommand.fromBson(b);
            if (!initEJB.validateSessionID(new String(adminCmd.getServerSessID()))) {
                    /* Cannot authenticate this request. */
                statusCode = WSResponse.FAILURE;
                msg = "Failed to authenticate request.";
            } else if (!initEJB.isIsInitialized()) {
                statusCode = WSResponse.FAILURE;
                msg = "Cannot create a session on the app server because it is not initialized.";
            } else {
                switch(adminCmd.getCommandName()) {
                    case "upgrade":
                        // Run the upgrade.
                        msg = upgradeCmd.run();
                        break;
                    case "refreshapp":
                        String client = adminCmd.getCommandArgs()[0];
                        Long appID = Long.parseLong(adminCmd.getCommandArgs()[1]);
                        appRegistry.refreshApplication(client, appID);
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to de-serialize admin command.", ex);
            statusCode = WSResponse.FAILURE;
            msg = (ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : ex.getMessage());
        } catch (AppserverSystemException ex) {
            LOG.log(Level.SEVERE, "Failed to update the app.", ex);
            statusCode = WSResponse.FAILURE;
            msg = ex.getLocalizedMessage();
        }
        
        if (msg == null) {
            msg = "Success";
            statusCode = WSResponse.SUCCESS;
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
