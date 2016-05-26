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

import com.mobilehelix.appserver.conn.ConnectionContainer;
import com.mobilehelix.appserver.constants.HTTPHeaderConstants;
import com.mobilehelix.appserver.ejb.ApplicationFacade;
import com.mobilehelix.appserver.ejb.ApplicationInitializer;
import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.settings.ApplicationSettings;
import com.mobilehelix.appserver.system.ApplicationServerRegistry;
import com.mobilehelix.appserver.system.ControllerConnectionBase;
import com.mobilehelix.appserver.system.InitApplicationServer;
import com.mobilehelix.services.objects.CreateSessionRequest;
import com.mobilehelix.services.objects.WSExtra;
import com.mobilehelix.services.objects.WSExtraGroup;
import com.mobilehelix.services.objects.WSUserPreference;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
    
    /* Global prefs tags. */
    public static final String PASSWORD_VAULT_PREFS_TAG = "password_vault";
    public static final String COPY_ON_CHECKOUT_TAG = "checkout_copy";
    
    private static final long MAX_DOWNLOAD_SIZE = 500 * 1024 * 1024;
    
    
    /* Global registry of application config downloaded from the Controller. */
    private ApplicationServerRegistry appRegistry;
    
    /* Global object that tracks app server properties and establishes the connection to the
     * Controller, if available.
     */
    private InitApplicationServer initAS;
    
    /* Map from appID to the app-specific facade for that app ID. */
    private final TreeMap<Long, ApplicationFacade> appFacades;    

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
    
    /* ID of this device. */
    private long deviceID;
    
    /* Base URL of the server servicing this request. */
    private String serverBaseURL;
    
    /* Client of this user. */
    private String client;
    
    /* Email address of this user. */
    private String userEmail;
    
    /* Legacy user ID of this user. */
    private String legacyUserID;
    
    /* List of app IDs in the session. */
    private final List<Long> appIDs;
    
    /* List of app gen IDs in the session. */
    private final List<Integer> appGenIDs;
        
    /* Map from app IDs to policies. */
    private final Map<Long, Collection<WSExtra> > policyMap;
    
    /* Map of connection objects stored in this session. */
    private final ConcurrentHashMap<String, ConnectionContainer> connMap;

    /* Map used to extend properties of session with app specific data */
    private final Map<String, Object> contextMap;

    /* Map from resource IDs to a list of user prefs. The special resource ID -1 is used
       for global prefs.
    */
    private final Map<Long, Set<WSUserPreference>> prefsMap;
    
    
    public Session(CreateSessionRequest createRequest) throws AppserverSystemException {
        this.appIDs = new LinkedList<>();
        this.appGenIDs = new LinkedList<>();
        this.contextMap = new HashMap<>();
        this.connMap = new ConcurrentHashMap<>();
        this.policyMap = new HashMap<>();
        this.appFacades = new TreeMap<>();
        this.prefsMap = new ConcurrentHashMap<>();
        this.deviceID = createRequest.getDeviceID();
        this.legacyUserID = createRequest.getLegacyUserID();
        this.userEmail = createRequest.getUserEmail();
        
        this.init(createRequest.getClient(), createRequest.getUserID(), createRequest.getPassword(), createRequest.getDeviceType(), false);
        // Do application-specific init for each application in the session.
        if (createRequest.getAppIDs() == null) {
            return;
        }
        
        // Capture prefs.
        if (createRequest.getUserSettings() != null) {
            for (WSUserPreference wuas : createRequest.getUserSettings()) {
                this.addPref(wuas.getResourceID(), wuas);
            }
        }
        
        // Initialize apps.
        this.doAppInit(createRequest.getAppIDs(), createRequest.getAppGenIDs(), createRequest.getAppProfiles());
    }
    
    public Session(String client, String username, String password) throws AppserverSystemException {
        // ONLY used for debugging.
        this.appIDs = new LinkedList<>();
        this.appGenIDs = new LinkedList<>();
        this.contextMap = new HashMap<>();
        this.connMap = new ConcurrentHashMap<>();
        this.policyMap = new HashMap<>();
        this.appFacades = new TreeMap<>();
        this.prefsMap = new ConcurrentHashMap<>();
        this.init(client, username, password, "iPhone", true);
    }
    
    public final void doAppInit(Long[] appIDs, 
            Integer[] appGenIDs, 
            List<WSExtraGroup> appProfiles) throws AppserverSystemException {        
        List<ApplicationSettings> sessApps = new LinkedList<>();
        for (int i = 0; i < appIDs.length; ++i) {
            Long appID = appIDs[i];
            Integer appGenID = appGenIDs[i];
            ApplicationSettings as = 
                    appRegistry.getSettingsForAppID(this.client, appID, appGenID);
            if (as == null) {
                /* The registration does not tell us the app type. Hence we may get
                 * normal web apps in our ID list. We just need to skip these ...
                 */
                continue;
            }
            
            sessApps.add(as);
            this.appIDs.add(appID);
            this.appGenIDs.add(appGenID);
        }
        
        if (appProfiles == null) {
            // Debug session
            try {
                // Now download all app policies for this session.
                this.policyMap.putAll(this.initAS.getControllerConnection().downloadAppPolicies(this));
            } catch (IOException ex) {
                throw new AppserverSystemException("Failed to load app policies.",
                        "SessionCannotLoadAppPolicies",
                        new String[] { ex.getMessage() });
            }
        } else {
            // Device session
            for (WSExtraGroup wseg : appProfiles) {
                this.policyMap.put(wseg.getId(), wseg.getExtras());
            }
        }
        
        // Now, with policies in hand, initialize all apps.
        if (!sessApps.isEmpty()) {
            this.executeAppInitTasks(sessApps);
        }
    }
    
    private void executeAppInitTasks(List<ApplicationSettings> sessApps) {
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(8, sessApps.size()));
        for (ApplicationSettings as : sessApps) {
            ApplicationFacade af = as.createFacade(this, this.appRegistry, false);
            if (af != null) {
                af.setInitStatus(executor.submit(new ApplicationInitializer(af, this, this.credentials)));
                af.setAppID(as.getAppID());
                this.appFacades.put(as.getAppID(), af);
            }
        }
    }
    
    public long getMaxDownloadSize() {
        // TODO:  make dynamic
        return MAX_DOWNLOAD_SIZE;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getLegacyUserID() {
        return legacyUserID;
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
            this.client = client;
            
            // Do a JNDI lookup of the app registry.
            InitialContext ictx = new InitialContext();
            java.lang.Object appRegObj =
                    ictx.lookup("java:global/ApplicationServerRegistry");
            appRegistry = (ApplicationServerRegistry)appRegObj;
            
            java.lang.Object initASObj =
                    ictx.lookup("java:global/InitApplicationServer");
            initAS = (InitApplicationServer)initASObj;
        } catch (NamingException ex) {
            LOG.log(Level.SEVERE, "Failed to initialize session.", ex);
            throw new AppserverSystemException(ex, "SessionInitializationFailed",
                    "Fatal error in session initialization.");
        }
    }

    public ApplicationSettings getCurrentApplication() {
        return currentApplication;
    }
    
    public ApplicationFacade getCurrentFacade() {
        return currentFacade;
    }
    
    public ApplicationFacade getCurrentFacade(int apptype) {
        if (this.currentFacade != null) {
            return this.currentFacade;
        }
        
        List<ApplicationFacade> allFs = this.getAllFacadesForType(apptype);
        if (allFs.isEmpty()) {
            return null;
        }
        return allFs.get(0);
    }
    
    public ApplicationFacade getCurrentFacade(HttpServletRequest req, int apptype) {
        String appid = this.getAppIDFromRequest(req);
        if (appid != null) {
            ApplicationFacade af = this.getAppFacade(Long.parseLong(appid));
            if (af.getAppType() == apptype) {
                return af;
            }
        } 
        List<ApplicationFacade> allFs = this.getAllFacadesForType(apptype);
        if (allFs.isEmpty()) {
            return null;
        }
        return allFs.get(0);
    }
    
    public ApplicationFacade getAppFacade(long appid) {
        return this.appFacades.get(appid);
    }
        
    public List<ApplicationFacade> getAllFacadesForType(int apptype) {
        List<ApplicationFacade> ret = new LinkedList<>();
        for (ApplicationFacade af : this.appFacades.values()) {
            if (af.getAppType() == apptype) {
                ret.add(af);
            }
        }
        return ret;
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
    
     // Find the record for the application this request is attempting to access. Return
     // true if the record is found, false if not. If not, the caller should redirect the
     //request to an error landing page.
    public ApplicationFacade initCurrentApplication(String appID, String appGenID,
            int apptype) throws AppserverSystemException {
        ApplicationFacade af = null;
        ApplicationSettings app = null;
        if (appID != null) {
            app = this.appRegistry.getSettingsForAppID(client, Long.parseLong(appID), Integer.parseInt(appGenID));
        } else if (debugOn) {
            app = this.appRegistry.getSettingsForApplicationType(client, apptype, this);
        } else {
            LOG.log(Level.SEVERE, "Received a null app ID in a non-debug session.");
        }
        
        if (app == null) {
            /* Could not lookup the application. Fail. */
            throw new AppserverSystemException("Failed to lookup current application in process request.",
                            "SessionCannotFindApp",
                            new Object[] {
                                appID != null ? appID : "null"
                            });
        }
        this.currentApplication = app;
        af = this.appFacades.get(app.getAppID());
        if (af == null) {
            /* Could not lookup the application. Fail. */
            throw new AppserverSystemException("Failed to lookup current facade in process request.",
                            "SessionCannotFindFacade");
        }
        this.currentFacade = af;
        return af;
    }
    
    public ApplicationFacade processRequest(Long appID, int appType) throws AppserverSystemException {
        if (appID == null) {
            throw new AppserverSystemException("Invalid app ID.", "InvalidAppID", new Object[]{ "null" });
        }
        
        /* Setup the application and facade. */
        ApplicationFacade af = this.initCurrentApplication(appID.toString(), Integer.toString(ApplicationServerRegistry.FORCE_NO_REFRESH), appType);        
        this.initCurrentFacade(false, af);
        return af;
    }
    
    /**
     * Should be called when a GET page request arrives.
     * @param req
     * @param appType
     * @return 
     * @throws com.mobilehelix.appserver.errorhandling.AppserverSystemException
     */
    public ApplicationFacade processRequest(HttpServletRequest req, int appType) 
            throws AppserverSystemException {        
        String reqUsername = req.getHeader(HTTPHeaderConstants.MH_FORMLOGIN_USERNAME_HEADER);
        String reqPassword = req.getHeader(HTTPHeaderConstants.MH_FORMLOGIN_PASSWORD_HEADER);

        boolean didChangeUser = false;
        boolean didChangePassword = false;
        
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
        String appID = this.getAppIDFromRequest(req);
        String appGenID = this.getAppGenFromRequest(req);
        ApplicationFacade af = this.initCurrentApplication(appID, appGenID, appType);        
        this.initCurrentFacade(didChangeUser || didChangePassword, af);
        return af;
    }

    
    public int initCurrentFacade(boolean reinit, ApplicationFacade af) throws AppserverSystemException {
        int status = 0;
        
        /* Block until the app facade init is done. This init is started when the session
         * is created.
         */
        try {
            status = af.getInitStatus();
        } catch (ExecutionException ee) {
            LOG.log(Level.SEVERE, "Exception collecting init status from the application facade.", ee);
            
            // Just re-throw so that the underlying error message is preserved.
            if (ee.getCause() instanceof AppserverSystemException) {
                throw (AppserverSystemException)ee.getCause();
            } else {
                throw new AppserverSystemException(ee,
                    "Asynchronous init failed.",
                    "SessionInitializationFailed",
                    new Object[] {
                        ee.getCause().getMessage()
                    });
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception collecting init status from the application facade.", e);
            throw new AppserverSystemException(e,
                "Asynchronous init failed.",
                "SessionInitializationFailed",
                new Object[] {
                    e.getMessage()
                });
        }
        
       /* If something substantial changed OR if this is the first load of the app., we re-init the facade. */
        if (!af.getInitOnLoadDone() || reinit) {
            /* Need to re-init the facade because credentials have changed. */
            af.doInitOnLoad(this, credentials);

            /* Indicate the first load is one. */
            af.setInitOnLoadDone();
        }
        
        return status;
    }

    public Object getProperty(String key) {
        return this.contextMap.get(key);
    }
    
    public void setProperty(String key, Object value) {
        this.contextMap.put(key, value);
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

    public long getDeviceID() {
        return deviceID;
    }

    public String getClient() {
        return client;
    }
    
    public Long[] getAppIDs() {
        if (this.appIDs == null) {
            return new Long[0];
        }
        
        Long[] ret = new Long[this.appIDs.size()];
        return this.appIDs.toArray(ret);
    }
    
    public Integer[] getAppGenIDs() {
        if (this.appGenIDs == null) {
            return new Integer[0];
        }
        Integer[] ret = new Integer[this.appGenIDs.size()];
        return this.appGenIDs.toArray(ret);
    }
    
    public WSExtra getPolicy(Long appID, String tag) {
        if (this.policyMap != null) {
            Collection<WSExtra> policyList = this.policyMap.get(appID);
            if (policyList == null) {
                return null;
            }
            
            for (WSExtra wse : policyList) {
                if (wse.getTag().equals(tag)) {
                    return wse;
                }
            }
        }
        return null;
    }
    
    // Return the session IDs of the child sessions created by this instance
    public void getChildren(List<byte[]> sessionIds) {
       byte[] szSessionId = (byte[]) this.getProperty("szSessionId");
       
       if (szSessionId != null)
           sessionIds.add(szSessionId);
    }
    
    public void close() {
        for (ApplicationFacade af : this.appFacades.values()) {
            af.close();
        }
        this.appFacades.clear();
        for (ConnectionContainer cc : this.connMap.values()) {
            cc.close();
        }
        this.connMap.clear();
    }
    
    public void getProperties(Map<String, Object> props) {
        this.initAS.getControllerConnection().getProperties(props);
    } 
    
    public ControllerConnectionBase getControllerConnection() {
        return this.initAS.getControllerConnection();
    }    
    
    public ConnectionContainer getConnectionForType(Class c) {
        return connMap.get(c.getName());
    }    
    
    public void saveConnectionForType(Class c,
            ConnectionContainer cc) {
        this.connMap.put(c.getName(), cc);
    }
    
    public final void addPref(Long resourceID, WSUserPreference pref) {
        if (pref == null) {
            return;
        }        
        if (resourceID == null) {
            resourceID = -1L;
        }
        Set<WSUserPreference> prefs = this.prefsMap.get(resourceID);
        if (prefs == null) {
            prefs = new HashSet<>();
            this.prefsMap.put(resourceID, prefs);
        } else {
            prefs.remove(pref);
        }
        
        prefs.add(pref);
    }
    
    public void removePref(Long resourceID, WSUserPreference pref) {
        if (pref == null) {
            return;
        }        
        if (resourceID == null) {
            resourceID = -1L;
        }
        Set<WSUserPreference> prefs = this.prefsMap.get(resourceID);
        if (prefs == null) {
           return;
        }
        prefs.remove(pref);
    }
        
    public WSUserPreference getPref(Long resourceID, String tag) {
        if (tag == null) {
            return null;
        }        
        if (resourceID == null) {
            resourceID = -1L;
        }
        WSUserPreference ret = null;
        Set<WSUserPreference> prefs = this.prefsMap.get(resourceID);
        if (prefs != null) {
            for (WSUserPreference pref : prefs) {        
                if (pref.getTag().equals(tag)) {
                    ret = pref;
                    break;
                }
            }
        }
        
        return ret;
    }
    
    public Set<WSUserPreference> getAllPrefs(Long resourceID) {
        return this.prefsMap.get(resourceID);
    }
}
