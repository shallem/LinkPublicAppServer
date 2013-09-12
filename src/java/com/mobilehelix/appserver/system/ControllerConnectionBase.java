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
package com.mobilehelix.appserver.system;

import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.services.objects.ApplicationServerInitRequest;

/**
 * When no Controller is available (e.g., in the open source edition of an app used
 * for demonstrating the SDK), this object acts as a proxy for the Controller connection.
 * 
 * @author shallem
 */
public class ControllerConnectionBase {
    public ControllerConnectionBase() {
        
    }
    
    public void refreshApplication(Long appID,
            Integer appGenID) throws AppserverSystemException {
        // Do nothing. We have no Controller.
    }
    
    public void processInitRequest(ApplicationServerInitRequest asir, String privIP) 
            throws AppserverSystemException {
        // Do nothing. We have no Controller.
    }
    
    /**
     * Derived classes should override; the purpose of this is to validate that an
     * incoming web services communication is coming from the Controller. When we 
     * have no Controller, we always return true. However, in a production setting
     * this would open up the Application Server to unauthenticated traffic. Override
     * this in your own applications.
     * 
     * @param incomingSessID
     * @return 
     */
    public boolean validateSessionID(String incomingSessID) {
        return true;
    }
    
    /**
     * In a multi server system, each should have a unique ID. The main purpose of these
     * IDs is to allow the Mobile Helix Controller to track the status of each server.
     * Again, not relevant in a test environment without the rest of the Mobile Helix
     * infrastructure.
     * 
     * @return Unique ID of this server, when a Controller is present.
     */
    public Long getServerID() {
        return (long)1;
    }
}
