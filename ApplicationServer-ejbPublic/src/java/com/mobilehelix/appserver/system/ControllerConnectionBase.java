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
import com.mobilehelix.appserver.session.GlobalPropertiesManager;
import com.mobilehelix.appserver.session.SessionManager;
import com.mobilehelix.services.objects.ApplicationServerInitRequest;

/**
 * When no Controller is available (e.g., in the open source edition of an app used
 * for demonstrating the SDK), this object acts as a proxy for the Controller connection.
 * 
 * @author shallem
 */
public class ControllerConnectionBase {
    // Repository for application configuration information.
    protected ApplicationServerRegistry appRegistry;
    
    // Global properties
    protected GlobalPropertiesManager globalProperties;
    
    // Global session manager
    protected SessionManager sessionMgr;
    
    public ControllerConnectionBase() {
        
    }
    
    public void refreshApplication(String client,
            Long appID,
            Integer appGenID) throws AppserverSystemException {
        // Do nothing. We have no Controller.
    }
    
    public void processInitRequest(ApplicationServerInitRequest asir, String privIP) 
            throws AppserverSystemException {
        /* Store the client name in the global properties. */
        this.globalProperties.setClientName(asir.getClientName());

        /* Store the pub/priv IP and port. */
        this.globalProperties.setAsPubIP(asir.getAsPubIP());
        this.globalProperties.setAsPubPort(asir.getAsPubPort());
        this.globalProperties.setAsPrivIP(privIP);
        this.globalProperties.setAsPrivPort(asir.getAsPrivPort());
        
        /* Reset the session manager. When we re-initialize the app server it is
         * no different than restarting the app server.
         */
        this.sessionMgr.sweepAllSessions();
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
     * infrastructure. The push server is another Mobile Helix server that is co-hosted
     * with the application server and is responsible for receiving push notifications
     * from Exchange.
     * 
     * @return Unique ID of this server, when a Controller is present.
     */
    public Long getServerID() {
        return (long)1;
    }

    public Long getPushServerID() {
        return (long)2;
    }
    
    public void setApplicationRegistry(ApplicationServerRegistry appRegistry) {
        this.appRegistry = appRegistry;
    }

    public void setGlobalProperties(GlobalPropertiesManager globalProperties) {
        this.globalProperties = globalProperties;
    }

    public void setSessionMgr(SessionManager sessionMgr) {
        this.sessionMgr = sessionMgr;
    }
}
