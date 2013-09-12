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
import com.mobilehelix.appserver.settings.ApplicationSettings;
import com.mobilehelix.appserver.settings.ApplicationSettingsFactory;
import com.mobilehelix.services.objects.WSApplication;
import java.util.List;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * Maps unique application IDs to application properties. The specific properties 
 * depend on the application type.
 *
 * @author shallem
 */
@Singleton
@Startup
@EJB(name="java:global/ApplicationServerRegistry", beanInterface=ApplicationServerRegistry.class)
public class ApplicationServerRegistry {
    // Indexed by app ID.
    private TreeMap<Long, ApplicationSettings> appMap;
    private TreeMap<Integer, ApplicationSettingsFactory> factoryMap;
    
    @EJB
    private InitApplicationServer initAS;
    
    private ControllerConnectionBase controllerConnection;
    
    @PostConstruct
    public void init() {
        this.appMap = new TreeMap<>();
        this.factoryMap = new TreeMap<>();
        this.controllerConnection = initAS.getControllerConnection();
    }
    
    public void addSettingsFactory(int appType, ApplicationSettingsFactory sf) {
        this.factoryMap.put(appType, sf);
    }
    
    public void processAppList(List<WSApplication> appList) {
        for (WSApplication wsa : appList) {
            ApplicationSettingsFactory sf = factoryMap.get(wsa.getAppType());
            if (sf == null) {
                // No factory; we cannot handle settings for this application in
                // this server.
                continue;
            }
            ApplicationSettings newAppSettings = sf.createInstance(wsa);
            if (newAppSettings == null) {
                // Can't handle the applicaton for some app-specific reason.
                continue;
            }
            
            if (appMap.containsKey(newAppSettings.getAppID())) {
                appMap.remove(newAppSettings.getAppID());
            }
            
            appMap.put(newAppSettings.getAppID(), newAppSettings);
        }
    }
    
    public ApplicationSettings getSettingsForAppID(Long appID) throws AppserverSystemException {
        return appMap.get(appID);
    }
    
    public ApplicationSettings getSettingsForAppID(Long appID, Integer appGenID) throws AppserverSystemException {
        // If we have a connection to the Controller, refresh the app first.
        controllerConnection.refreshApplication(appID, appGenID);
        return appMap.get(appID);
    }
    
    /**
     * Used while debugging. Get the first application settings object of the right type.
     * @param apptype
     * @return 
     */
    public ApplicationSettings getSettingsForApplicationType(int apptype) {
        for (ApplicationSettings appSettings : this.appMap.values()) {
            if (appSettings.getAppType() == apptype) {
                return appSettings;
            }
        }
        
        return null;
    }
    
    /**
     * Delete an app from the app registry. Intended to be called when a message is received
     * indicating that an object has been deleted from the Controller.
     * 
     * @param appID 
     */
    public void deleteAppFromRegistry(Long appID) {
        appMap.remove(appID);
    }
}
