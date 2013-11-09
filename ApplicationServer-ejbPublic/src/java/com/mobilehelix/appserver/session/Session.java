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

import com.mobilehelix.appserver.constants.HTTPHeaderConstants;
import com.mobilehelix.appserver.ejb.ApplicationFacade;
import com.mobilehelix.appserver.ejb.ApplicationInitializer;
import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.settings.ApplicationSettings;
import com.mobilehelix.appserver.system.ApplicationServerRegistry;
import com.mobilehelix.services.objects.ApplicationServerCreateSessionRequest;
import java.text.MessageFormat;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author shallem
 */
public class Session {

    private static final Logger LOG = Logger.getLogger(Session.class.getName());
    
    /**
     * The lock is used to ensure that only one thread has access to this session
     * at a time. Many of the client applications will use multiple threads to access
     * different application server services. Unfortunately, it is common for these
     * services to use NTLM, and NTLM triggers a call to active directory each time
     * an authentication occurs. If >2 threads contact A-D simultaneously then a user
     * is locked out of A-D. Hence, this lock can be used to protect operations that
     * might trigger a call to A-D via an NTLM authentication.
     */
    private final ReentrantLock lock = new ReentrantLock();
    
    /* Global registry of application config downloaded from the Controller. */
    private ApplicationServerRegistry appRegistry;
    
    /* Map from appID to the app-specific facade for that app ID. */
    private TreeMap<Long, ApplicationFacade> appFacades;    

    /* Full settings object for the current application. */
    private ApplicationSettings currentApplication;

    /* Current application facade. */
    private ApplicationFacade currentFacade;
    
    /* Credentials. */
    private CredentialsManager credentials;
    
    /* Is debugging on? */
    private boolean debugOn;
    
    /* What type of device is this? */
    private String deviceType;
    
    /* Base URL of the server servicing this request. */
    private String serverBaseURL;
        
    public Session(ApplicationServerCreateSessionRequest sess, 
            ApplicationInitializer appInit) throws AppserverSystemException {
        this.init(sess.getClient(), sess.getUserID(), sess.getPassword(), sess.getDeviceType(), false);
        // Do application-specific init for each application in the session.
        if (sess.getAppIDs() == null) {
            return;
        }
        
        this.doAppInit(sess.getAppIDs(), sess.getAppGenIDs(), appInit);
    }
    
    public Session(String username, String password, boolean debugOn) throws AppserverSystemException {
        this.init("", username, password, "iPhone", debugOn);
    }
    
    public final void doAppInit(Long[] appIDs, 
            Integer[] appGenIDs, 
            ApplicationInitializer appInit) throws AppserverSystemException {
        for (int i = 0; i < appIDs.length; ++i) {
            Long appID = appIDs[i];
            Integer appGenID = appGenIDs[i];
            ApplicationSettings as = 
                    appRegistry.getSettingsForAppID(appID, appGenID);
            if (as == null) {
                /* The registration does not tell us the app type. Hence we may get
                 * normal web apps in our ID list. We just need to skip these ...
                 */
                continue;
            }
            
            ApplicationFacade af = as.createFacade(appRegistry, false);
            af.setInitStatus(appInit.doInit(af, this.credentials));
            af.setAppID(appID);
            this.appFacades.put(as.getAppID(), af);
        }        
    }
    
    private void init(String client,
            String username, 
            String password,
            String deviceType,
            boolean debugOn) throws AppserverSystemException {
        try {
            // For now hard code values.
            credentials = new CredentialsManager(client, username, password);
            this.debugOn = debugOn;
            this.deviceType = deviceType;
            this.appFacades = new TreeMap<>();
            
            // Do a JNDI lookup of the app registry.
            InitialContext ictx = new InitialContext();
            java.lang.Object appRegObj =
                    ictx.lookup("java:global/ApplicationServerRegistry");
            appRegistry = (ApplicationServerRegistry)appRegObj;
        } catch (NamingException ex) {
            LOG.log(Level.SEVERE, "Failed to initialize session.", ex);
            throw new AppserverSystemException(ex, "SessionInitializationFailed",
                    "Fatal error in session initialization.");
        }
    }

    public ApplicationFacade getCurrentFacade() {
        return currentFacade;
    }
    
    public ApplicationFacade getAppFacade(long appid) {
        return this.appFacades.get(appid);
    }
    
    public ApplicationFacade getFacadeForType(int apptype) {
        for (ApplicationFacade af : this.appFacades.values()) {
            if (af.getAppType() == apptype) {
                return af;
            }
        }
        return null;
    }
    
    private String getValueFromCookieName(HttpServletRequest req,
            String cookieNamePrefix) {
        String appID = req.getHeader(cookieNamePrefix);
        if ((appID == null) && (req.getCookies() != null)) {
            for (Cookie c : req.getCookies()) {
                if (c.getName().equals(cookieNamePrefix)) {
                    appID = c.getValue();
                    break;
                }
            }
        }
        
        return appID;
    }
    
    private String getAppIDFromRequest(HttpServletRequest req) {
        return this.getValueFromCookieName(req, HTTPHeaderConstants.MH_APP_ID_HEADER);
    }
    
    private String getAppGenFromRequest(HttpServletRequest req) {
        return this.getValueFromCookieName(req, HTTPHeaderConstants.MH_APP_GEN_HEADER);
    }
    
    /**
     * Find the record for the application this request is attempting to access. Return
     * true if the record is found, false if not. If not, the caller should redirect the
     * request to an error landing page.
     * 
     * @param req
     * @param apptype
     * @return 
     */
    private void findApplication(HttpServletRequest req,
            int apptype) throws AppserverSystemException {
        
        String appID = this.getAppIDFromRequest(req);
        String appGenID = this.getAppGenFromRequest(req);

        if (this.currentApplication != null) {
            /* Make sure the app didn't change ... */
            if (appID != null &&
                    this.currentApplication.getAppID() == Long.parseLong(appID)) {
                /* Make sure the currentFacade is for the current app. */
                this.currentFacade = this.appFacades.get(this.currentApplication.getAppID());
                
                /* Have already found the application and facade. */
                return;
            }
            
            /* If we are debugging, match by type. */
            if (debugOn &&
                    this.currentApplication.getAppType() == apptype) {
                /* Make sure the currentFacade is for the current app. */
                this.currentFacade = this.appFacades.get(this.currentApplication.getAppID()); 
                
                /* Have already found the application and facade. */
                return;
            }
            
            /* If we fall through then we have changed applications. */
        }
        
        if (appID != null && appGenID != null) {
            // Get the per-app configuration.
            this.currentApplication =
                appRegistry.getSettingsForAppID(Long.parseLong(appID), Integer.parseInt(appGenID));
        } else if (this.debugOn && 
                (appID == null || appGenID == null)) {
            // Install defaults. For now (while debugging), look up config by type.
            this.currentApplication =
                    appRegistry.getSettingsForApplicationType(apptype);
        }
        
        if (this.currentApplication != null) {
            // Whenever we change apps, we reset the current facade.
            this.currentFacade = this.appFacades.get(this.currentApplication.getAppID());
        }
    }
    
    /**
     * Should be called when a GET page request arrives.
     */
    public void processRequest(HttpServletRequest req, int apptype) 
            throws AppserverSystemException {        
        String reqUsername = req.getHeader(HTTPHeaderConstants.MH_FORMLOGIN_USERNAME_HEADER);
        String reqPassword = req.getHeader(HTTPHeaderConstants.MH_FORMLOGIN_PASSWORD_HEADER);

        boolean didChangeUser = false;
        boolean didChangePassword = false;
        boolean didCreate = false;
        
        /* Handle operations that are fully encapsulated in the request first. */
        if (reqUsername != null) {
            didChangeUser = credentials.setRequestUsername(reqUsername);
        }
        if (reqPassword != null) {
            didChangePassword = credentials.setRequestPassword(reqPassword);
        }
    
        /* Save off the base URL. */
        this.serverBaseURL = 
            MessageFormat.format("{0}://{1}:{2}",
            new Object[]{
                req.getScheme(),
                req.getServerName(),
                Integer.toString(req.getServerPort())
            });   
        
        /* Setup the application and facade. */
        this.findApplication(req, apptype);
        
        if (this.currentApplication == null) {
            /* Could not lookup the application. Fail. */
            throw new AppserverSystemException("Failed to lookup current application in process request.",
                            "SessionCannotFindApp");
        }
        
        if (this.currentFacade == null) {
            /* First time we have been here ... */
            didCreate = true;
                
            /* This will generally only happen in debug sessions. */
            this.currentFacade = this.currentApplication.createFacade(appRegistry, debugOn);
            
            /* Store the mapping from app ID to facade. */
            this.appFacades.put(this.currentApplication.getAppID(), this.currentFacade);
        } 
        
        if (!didCreate) {
            /* Block until the app facade init is done. This init is started when the session
             * is created.
             */
            Integer status = this.currentFacade.getInitStatus();
        }
        
        /* If something substantial changed OR if this is the first load of the app., we re-init the facade. */
        if (!this.currentFacade.getInitOnLoadDone() || didChangeUser || didChangePassword) {
            /* Need to re-init the facade because credentials have changed. */
            this.currentFacade.doInitOnLoad(req, credentials);
            
            /* Inidicate the first load is one. */
            this.currentFacade.setInitOnLoadDone();
        }   
    }

    public String getServerBaseURL() {
        return serverBaseURL;
    }
    
    public CredentialsManager getCredentials() {
        return credentials;
    }

    public String getDeviceType() {
        return deviceType;
    }
    
    public void close() {
        for (ApplicationFacade af : this.appFacades.values()) {
            af.close();
        }
    }
}
