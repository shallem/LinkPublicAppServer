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
import com.mobilehelix.appserver.session.Session;
import com.mobilehelix.appserver.settings.ApplicationSettings;
import com.mobilehelix.appserver.settings.ApplicationSettingsFactory;
import com.mobilehelix.services.objects.WSApplication;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
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
@PermitAll
@EJB(name="java:global/ApplicationServerRegistry", beanInterface=ApplicationServerRegistry.class)
public class ApplicationServerRegistry {
    // Indexed by client, then app ID.
    private TreeMap<String, TreeMap<Long, ApplicationSettings> > appMap;
    private TreeMap<Integer, ApplicationSettingsFactory> factoryMap;
    
    @EJB
    private InitApplicationServer initAS;
    
    private ControllerConnectionBase controllerConnection;
    
    @PostConstruct
    public void init() {
        this.appMap = new TreeMap<>();
        this.factoryMap = new TreeMap<>();
        this.controllerConnection = initAS.getControllerConnection();
        this.controllerConnection.setApplicationRegistry(this);
    }
    
    public void addSettingsFactory(int appType, ApplicationSettingsFactory sf) {
        this.factoryMap.put(appType, sf);
    }
    
    public void processAppList(String client,
            List<WSApplication> appList) {
        TreeMap<Long, ApplicationSettings> cliMap = appMap.get(client);
        if (cliMap == null) {
            cliMap = new TreeMap<>();
            appMap.put(client, cliMap);
        }
        
        for (WSApplication wsa : appList) {
            ApplicationSettingsFactory sf = factoryMap.get(wsa.getAppType());
            if (sf == null) {
                // No factory; we cannot handle settings for this application in
                // this server.
                continue;
            }
            ApplicationSettings newAppSettings = sf.createInstance(client, wsa);
            if (newAppSettings == null) {
                // Can't handle the applicaton for some app-specific reason.
                continue;
            }
            
            if (cliMap.containsKey(newAppSettings.getAppID())) {
                cliMap.remove(newAppSettings.getAppID());
            }
            
            cliMap.put(newAppSettings.getAppID(), newAppSettings);
        }
    }
    
    public ApplicationSettings getSettingsForAppID(String client, Long appID) throws AppserverSystemException {
        TreeMap<Long, ApplicationSettings> cliMap = appMap.get(client);
        if (cliMap == null) {
            return null;
        }
        return cliMap.get(appID);
    }
    
    public ApplicationSettings getSettingsForAppID(String client, Long appID, Integer appGenID) throws AppserverSystemException {
        // If we have a connection to the Controller, refresh the app first.
        controllerConnection.refreshApplication(client, appID, appGenID);
        TreeMap<Long, ApplicationSettings> cliMap = appMap.get(client);
        if (cliMap == null) {
            return null;
        }
        return cliMap.get(appID);
    }
    
    /**
     * Get the first application settings object of the right type that is contained
     * within the supplied session.
     * 
     * @param apptype
     * @return 
     */
    public ApplicationSettings getSettingsForApplicationType(String client, 
            int apptype,
            Session sess) {
        TreeMap<Long, ApplicationSettings> cliMap = appMap.get(client);
        if (cliMap == null) {
            return null;
        }
        
        Set<Long> appIDSet = new TreeSet<>();
        appIDSet.addAll(Arrays.asList(sess.getAppIDs()));
        for (ApplicationSettings appSettings : cliMap.values()) {
            if (appSettings.getAppType() == apptype &&
                    appIDSet.contains(appSettings.getAppID())) {
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
    public void deleteAppFromRegistry(String client,
            Long appID) {
        TreeMap<Long, ApplicationSettings> cliMap = appMap.get(client);
        if (cliMap == null) {
            return;
        }
        cliMap.remove(appID);
    }
    
    public Long[] getAppIDs(String client) {
        TreeMap<Long, ApplicationSettings> cliMap = appMap.get(client);
        if (cliMap == null) {
            return null;
        }
        Long[] ret = new Long[cliMap.keySet().size()];
        return cliMap.keySet().toArray(ret);
    }
    
    public Integer[] getAppGenIDs(String client) {
        TreeMap<Long, ApplicationSettings> cliMap = appMap.get(client);
        if (cliMap == null) {
            return null;
        }
        Integer[] ret = new Integer[cliMap.keySet().size()];
        int i = 0;
        for (ApplicationSettings as : cliMap.values()) {
            ret[i++] = as.getAppGenID();
        }
        return ret;
    }
}
