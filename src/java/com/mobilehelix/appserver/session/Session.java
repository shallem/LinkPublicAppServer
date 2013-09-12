/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.session;

import com.mobilehelix.appserver.connections.MHConnectException;
import com.mobilehelix.appserver.constants.HTTPHeaderConstants;
import com.mobilehelix.appserver.ejb.ApplicationFacade;
import com.mobilehelix.appserver.ejb.ApplicationInitializer;
import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.settings.ApplicationSettings;
import com.mobilehelix.appserver.system.ApplicationServerRegistry;
import com.mobilehelix.services.objects.CreateSessionRequest;
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
        
    public Session(CreateSessionRequest sess, 
            ApplicationInitializer appInit) throws AppserverSystemException, MHConnectException {
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
            ApplicationInitializer appInit) throws AppserverSystemException, MHConnectException {
        for (int i = 0; i < appIDs.length; ++i) {
            Long appID = appIDs[i];
            Integer appGenID = appGenIDs[i];
            ApplicationSettings as = 
                    appRegistry.getSettingsForApplication(appID, appGenID);
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
    
    private String getAppIDFromReqeust(HttpServletRequest req) {
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
    public boolean findApplication(HttpServletRequest req,
            int apptype) throws AppserverSystemException {
        
        String appID = this.getAppIDFromReqeust(req);
        String appGenID = this.getAppGenFromRequest(req);

        if (this.currentApplication != null) {
            /* Make sure the app didn't change ... */
            if (appID != null &&
                    this.currentApplication.getAppID() == Long.parseLong(appID)) {
                /* Have already found the application. */
                return true;
            }
            
            /* If we are debugging, match by type. */
            if (debugOn &&
                    this.currentApplication.getAppType() == apptype) {
                return true;
            }
            
            /* If we fall through then we have changed applications. */
        }
        
        if (this.debugOn && 
                (appID == null || appGenID == null)) {
            // Install defaults. For now (while debugging), look up config by type.
            this.currentApplication =
                    appRegistry.getSettingsForApplicationType(apptype);
            // Whenever we change apps, we reset the current facade.
            this.currentFacade = null;
        } else if (appID != null && appGenID != null) {
            // Get the per-app configuration.
            this.currentApplication =
                appRegistry.getSettingsForApplication(Long.parseLong(appID), Integer.parseInt(appGenID));
            // Whenever we change apps, we reset the current facade.
            this.currentFacade = null;
        }

        return (this.currentApplication != null);
    }
    
    /**
     * Should be called when a GET page request arrives.
     */
    public void processRequest(HttpServletRequest req) 
            throws AppserverSystemException, MHConnectException {        
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
        
        /* See if we have an application facade. If not, create one. If we can't create one,
         * just return so that operations that don't require an application facade can 
         * proceed.
         */
        ApplicationFacade af = this.currentFacade;
        if (af == null) {
            /* First time we have been here or we just switched from another app ... */
            if (this.currentApplication != null) {
                didCreate = true;
                af = this.appFacades.get(this.currentApplication.getAppID());
            
                if (af == null) {
                    /* This will generally only happen in debug sessions. */
                    this.currentFacade = af = this.currentApplication.createFacade(appRegistry, debugOn);
                } else {
                    this.currentFacade = af;
                }
            } else {
                return;
            }
        } 
        
        if (!didCreate) {
            /* Block until the app facade init is done. */
            Integer status = af.getInitStatus();
        }
        
        /* If something substantial changed, we re-init the facade. */
        if (didCreate || didChangeUser || didChangePassword) {
            /* Need to re-init the facade because credentials have changed. */
            af.doInitOnLoad(req, credentials);
        
            if (didCreate) {
                this.appFacades.put(this.currentApplication.getAppID(), this.currentFacade);
            }
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
}
