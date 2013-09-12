/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.system;

import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.settings.ApplicationSettings;
import com.mobilehelix.appserver.settings.EmailApplicationSettings;
import com.mobilehelix.appserver.settings.FileBrowserApplicationSettings;
import com.mobilehelix.appserver.settings.JiraApplicationSettings;
import com.mobilehelix.appserver.settings.SharepointApplicationSettings;
import com.mobilehelix.constants.ApplicationConstants;
import com.mobilehelix.services.objects.ListApplicationResponse;
import com.mobilehelix.services.objects.ServerRegisterResponse;
import com.mobilehelix.services.objects.WSApplication;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.io.IOException;
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
    // Reference to the init object.
    @EJB
    private InitApplicationServer initAppServer;
    
    // Indexed by app ID.
    private TreeMap<Long, ApplicationSettings> appMap;
    
    @PostConstruct
    public void init() {
        this.appMap = new TreeMap<>();
    }
    
    private void initFromAppList(List<WSApplication> appList) {
        for (WSApplication wsa : appList) {
            ApplicationSettings newAppSettings;
            switch(wsa.getAppType()) {
                case ApplicationConstants.APPLICATION_TYPE_EMAIL:
                    newAppSettings = new EmailApplicationSettings(wsa);
                    break;
                case ApplicationConstants.APPLICATION_TYPE_FILE_BROWSER:
                    newAppSettings = new FileBrowserApplicationSettings(wsa);
                    break;
                case ApplicationConstants.APPLICATION_TYPE_SHAREPOINT:
                    newAppSettings = new SharepointApplicationSettings(wsa);
                    break;
                case ApplicationConstants.APPLICATION_TYPE_JIRA:
                    newAppSettings = new JiraApplicationSettings(wsa);
                    break;
                default:
                    // Application type we can't handle. Skip it.
                    continue;
            }
            if (appMap.containsKey(newAppSettings.getAppID())) {
                appMap.remove(newAppSettings.getAppID());
            }
            
            appMap.put(newAppSettings.getAppID(), newAppSettings);
        }
    }
    
    public void initFromRegisterResponse(ServerRegisterResponse srr) {
        this.initFromAppList(srr.getApplications());
    }
    
    public void updateFromAppRefreshResponse(ListApplicationResponse lar) {
        this.initFromAppList(lar.getApps());
    }
    
    public ApplicationSettings getSettingsForApplication(Long appID,
            Integer appGenID) throws AppserverSystemException {
        ApplicationSettings appSettings =
                appMap.get(appID);
        if (appSettings == null || appSettings.getAppGenID() < appGenID) {
            try {
                ListApplicationResponse lar =
                        initAppServer.refreshFromController(appID);
                this.updateFromAppRefreshResponse(lar);

                // Get the application settings again (post refresh)
                appSettings = appMap.get(appID);
            } catch (UniformInterfaceException | IOException ex) {
                throw new AppserverSystemException(ex,
                        "Application list refresh failed.",
                        "ApplicationRefreshFailed",
                        new Object[]{ ex.getLocalizedMessage() });
            }
        }
        
        return appSettings;
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
