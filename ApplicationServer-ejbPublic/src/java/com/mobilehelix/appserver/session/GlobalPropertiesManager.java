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
package com.mobilehelix.appserver.session;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * Used to read global properties and, when used without a Controller, global app
 * configuration.
 * 
 * @author shallem
 */
@Startup
@Singleton
@PermitAll
public class GlobalPropertiesManager {
    /* Main client name. */
    private String clientName;
    
    /* Debugging properties. */
    private boolean debugOn;
    private String debugUser;
    private String debugPassword;
    
    @PostConstruct
    public void init() {
        this.readDebugProperties();
    }
    
    private void readDebugProperties() {
        String debugOnStr = System.getProperty("com.mobilehelix.debug");
        if (debugOnStr != null && debugOnStr.equals("true")) {
            this.debugOn = true;
            this.debugUser = System.getProperty("com.mobilehelix.debuguser");
            this.debugPassword = System.getProperty("com.mobilehelix.debugpassword");
        } else {
            this.debugOn = false;
            this.debugUser = null;       
            this.debugPassword = null;
        }
    }
    
    public boolean isDebugOn() {
        return debugOn;
    }

    public String getDebugUser() {
        return debugUser;
    }
    
    public String getDebugPassword() {
        return debugPassword;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}
