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

import com.mobilehelix.appserver.system.InitApplicationServer;
import com.mobilehelix.services.interfaces.WSResponse;
import com.mobilehelix.services.objects.ApplicationServerInitRequest;
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
@Path("/initas")
@PermitAll
public class InitAppserverWS {
    private static final Logger LOGGER = Logger
        .getLogger(InitAppserverWS.class.getName());    
    
    @EJB
    private InitApplicationServer initEJB;
    
    @POST
    public byte[] InitAS(byte [] b) {
        int statusCode = WSResponse.FAILURE;
        String msg = null;

        ApplicationServerInitRequest asir = null;
        
        try {
            asir = ApplicationServerInitRequest.fromBson(b);
            msg = initEJB.processInitRequest(asir, 0, true);
            statusCode = WSResponse.SUCCESS;
        } catch(Exception e) {
                LOGGER.log(Level.SEVERE, "App server init failed with exception.", e);
            msg = e.getLocalizedMessage();
            statusCode = WSResponse.FAILURE;
            
            if (msg == null) {
                msg = "Unspecified failure ("+e.getClass().getSimpleName()+")";
            }
        }
        
        GenericBsonResponse gbr = new GenericBsonResponse(statusCode, msg);
        try {
            return gbr.toBson();
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Failed to serialize gateway cert response.", ioe);
        }
        return null;
    }
}
