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
    // Special app Gen ID value used to indicate that this app should NOT be refreshed from the Controller.
    public static final int FORCE_NO_REFRESH = -1;
    public static final int FORCE_REFRESH = -2;
    
    // Indexed by client, then app ID.
    private TreeMap<String, TreeMap<Long, WSApplication> > appResponseMap;
    private TreeMap<String, TreeMap<Long, ApplicationSettings> > appMap;
    private TreeMap<Integer, ApplicationSettingsFactory> factoryMap;
    private TreeMap<String, TreeSet<Long>> ignoreMap; 
    
    private ControllerConnectionBase controllerConnection;
    
    @PostConstruct
    public void init() {
        this.appMap = new TreeMap<>();
        this.factoryMap = new TreeMap<>();
        this.ignoreMap = new TreeMap<>();
        this.appResponseMap = new TreeMap<>();
    }
    
    public void setControllerConnection(ControllerConnectionBase b) {
        this.controllerConnection = b;
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
        TreeMap<Long, WSApplication> respMap = appResponseMap.get(client);
        if (respMap == null) {
            respMap = new TreeMap<>();
            appResponseMap.put(client, respMap);
        }
        
        for (WSApplication wsa : appList) {
            respMap.put(wsa.getUniqueID(), wsa);
            ApplicationSettingsFactory sf = factoryMap.get(wsa.getAppType());
            if (sf == null) {
                // No factory; we cannot handle settings for this application in
                // this server. However, we want to track this app ID so that we never
                // try to download settings for it ...
                TreeSet<Long> ignoreSet = ignoreMap.get(client);
                if (ignoreSet == null) {
                    ignoreSet = new TreeSet<>();
                    ignoreMap.put(client, ignoreSet);
                }
                ignoreSet.add(wsa.getUniqueID());
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
    
    public WSApplication getResponseForAppID(String client, Long appID) {
        TreeMap<Long, WSApplication> respMap = appResponseMap.get(client);
        if (respMap == null) {
            return null;
        }
        return respMap.get(appID);
    }
    
    public ApplicationSettings getSettingsForAppID(String client, Long appID) {
        TreeMap<Long, ApplicationSettings> cliMap = appMap.get(client);
        if (cliMap == null) {
            return null;
        }
        return cliMap.get(appID);
    }
    
    public void refreshApplication(String client, Long appID) throws AppserverSystemException {
        // If we have a connection to the Controller, refresh the app first.
        if (controllerConnection != null) {
            controllerConnection.refreshApplication(client, appID, ApplicationServerRegistry.FORCE_REFRESH);
        }
        
    }
    
    public ApplicationSettings getSettingsForAppID(String client, Long appID, Integer appGenID) throws AppserverSystemException {
        // Lookup the application first and see if the appGenID matches the one provided.
        if (appID != null && appGenID != null) {
            ApplicationSettings s = this.getSettingsForAppID(client, appID);
            if (s != null && (appGenID == ApplicationServerRegistry.FORCE_NO_REFRESH ||
                    s.getAppGenID().equals(appGenID))) {
                return s;
            } else if (s == null) {
                // Check the ignore map.
                TreeSet<Long> ignoreSet = ignoreMap.get(client);
                if (ignoreSet != null && ignoreSet.contains(appID)) {
                    return null;
                }
            }
        }
        
        // If we have a connection to the Controller, refresh the app first.
        if (controllerConnection != null) {
            controllerConnection.refreshApplication(client, appID, appGenID);
        }
        
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
     * @param client
     * @param apptype
     * @param sess
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
                    appIDSet.contains(appSettings.getAppID()) &&
                    appSettings.isIsVisibleOnDevice()) {
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
    
    public boolean pingAllApplications(List<String> warningMsgs,
            String client) {
        TreeMap<Long, ApplicationSettings> cliMap = appMap.get(client);
        boolean didFail = false;
        if (cliMap == null) {
            return true;
        }
        // Meant to hold a free form tag that resources within an appsettings object can
        // use to decide if the ping operation has already been completed in this cycle. We need
        // this mechanism because ping targets (e.g., agent servers) are generally associated with
        // a resource, but many apps can have the same resource. We don't want to ping once per appearance
        // of a resource on each ping cycle.
        Set<String> completedPings = new TreeSet<>();
        for (ApplicationSettings as : cliMap.values()) {
            if (!as.doPing(warningMsgs, completedPings)) {
                didFail = true;
            }
        }
        return !didFail;
    }
}
