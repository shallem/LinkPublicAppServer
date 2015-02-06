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

import com.mobilehelix.appserver.system.GlobalPropertiesManager;
import com.mobilehelix.appserver.constants.HTTPHeaderConstants;
import com.mobilehelix.appserver.ejb.ApplicationInitializer;
import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.push.PushManager;
import com.mobilehelix.appserver.system.InitApplicationServer;
import com.mobilehelix.services.objects.ApplicationServerCreateSessionRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;

/**
 * Responsible for getting active sessions from the database. Also maintains a
 * cache of active sessions for fast access. Right now this cache is of unlimited
 * size, although sessions are periodically kicked out when they are deemed to
 * have expired.
 *
 * @author shallem
 */
@Startup
@Singleton
@EJB(name="java:global/SessionManager", beanInterface=SessionManager.class)
public class SessionManager {
    private static final Logger LOG = Logger.getLogger(SessionManager.class.getName());
    
    private ConcurrentHashMap<String, Session> globalSessionMap;
    private Session debugSession;

    /* EJB to perform async init on application settings. */
    @EJB
    private ApplicationInitializer appInit;
    
    /* Global properties. */
    @EJB
    private GlobalPropertiesManager globalProperties;
    
    /* App server init object. */
    @EJB
    private InitApplicationServer initAS;
    
    /* Used when creating debug sessions ... */
    @EJB
    private PushManager pushMgr;
    
    @PostConstruct
    public void init() {
        globalSessionMap = new ConcurrentHashMap<>();
        
        // Determine what type of app registry we have ...
    }

    public String hashSessionID(byte[] sessionKey) throws AppserverSystemException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(sessionKey);
            return Base64.encodeBase64String(md.digest());
        } catch(NoSuchAlgorithmException ex) {
            LOG.log(Level.SEVERE, "Failed to hash session key.", ex);
            throw new AppserverSystemException("Failed to generate session ID hash.",
                    "SessionIDHashingFailed");
        }
    }
    
    public Session addSession(ApplicationServerCreateSessionRequest sess)
            throws AppserverSystemException {
        String sessIDB64 = this.hashSessionID(sess.getSessionKey());
        Session appServerSession = new Session(sess, appInit);
        globalSessionMap.put(sessIDB64, appServerSession);
        return appServerSession;
    }

    /*public void doSessionInit(Session sess, Long[] appIDs, Integer[] appGenIDs)
            throws AppserverSystemException {
        sess.doAppInit(appIDs, appGenIDs, appInit);
    }*/

    public boolean deleteSession(byte[] sessionHash) {
        boolean res = true;
        String sessIDB64 = Base64.encodeBase64String(sessionHash);        
        Session s = this.globalSessionMap.get(sessIDB64);

        if (s != null) {
            // If this session created child sessions, sweep the children as well
            List<byte[]> childrenIds = new ArrayList<>();
            s.getChildren(childrenIds);
            
            for (byte[] id : childrenIds)
                this.deleteSession(id);
            
            s.close();
            this.globalSessionMap.remove(sessIDB64);
        } else {
            LOG.log(Level.WARNING, "Session with id [" + sessIDB64 + "] not found");
            res = false;
        }
        
        return res;
    }

    private String getSessIDFromRequest(HttpServletRequest req) throws AppserverSystemException {
        String sessIDB64 = req.getHeader(HTTPHeaderConstants.MH_SESSION_ID_HEADER);
        if ((sessIDB64 == null)  && (req.getCookies() != null)) {
            for (Cookie c : req.getCookies()) {
                if (HTTPHeaderConstants.MH_SESSION_ID_HEADER.equalsIgnoreCase(c.getName())) {
                    sessIDB64 = c.getValue();
                    break;
                }
            }
        }
        // Decode the B64-encoded key.
        if (sessIDB64 != null) {
            byte[] sessID = Base64.decodeBase64(sessIDB64);
            return this.hashSessionID(sessID);
        }
        return null;
    }

    public void createDebugSession() throws AppserverSystemException {
        List<Long> appIDs = new LinkedList<>();
        List<Integer> appGenIDs = new LinkedList<>();
        this.debugSession = new Session(globalProperties.getClientName(),
            this.getDebugUser(),
            this.getDebugPassword());
                    
        initAS.getControllerConnection().refreshApplications(globalProperties.getClientName(), 
            this.debugSession.getCredentials().getUsernameNoDomain(), appIDs, appGenIDs);
        initAS.getControllerConnection().refreshUserPrefs(globalProperties.getClientName(), 
                this.debugSession.getCredentials().getUsernameNoDomain(), null, debugSession);
        
        Long[] appIDsArr = new Long[appIDs.size()];
        Integer[] appGenIDsArr = new Integer[appGenIDs.size()];
        appIDsArr = appIDs.toArray(appIDsArr);
        appGenIDsArr = appGenIDs.toArray(appGenIDsArr);
        
        this.debugSession.doAppInit(appIDsArr, appGenIDsArr, null, appInit);
                    
        // Also create a push session.
        this.pushMgr.addSession(appIDsArr, appGenIDsArr, globalProperties.getClientName(), this.getDebugUser(), this.getDebugPassword(), "iPad Air");
    }
    
    public Session getSessionForRequest(HttpServletRequest req) throws AppserverSystemException {
        String sessIDB64 = this.getSessIDFromRequest(req);
        if (sessIDB64 == null) {
            if (this.isDebugOn()) {
                if (this.debugSession == null) {
                    this.createDebugSession();
                }
                return this.debugSession;
            }
            return null;
        }
        return globalSessionMap.get(sessIDB64);
    }

    public Session getSessionForID(String sessIDB64) {
        return globalSessionMap.get(sessIDB64);
    }

    public void sweepSessions(List<String> idsToSweep) {
        for (String s : idsToSweep) {
            globalSessionMap.remove(s);
        }
    }

    public void sweepAllSessions() {
        globalSessionMap.clear();
    }

    public boolean isDebugOn() {
        return globalProperties.isDebugOn();
    }

    public String getDebugUser() {
        return globalProperties.getDebugUser();
    }

    public String getDebugPassword() {
        return globalProperties.getDebugPassword();
    }

    public Session getDebugSession() {
        return debugSession;
    }

    public void setDebugSession(Session debugSession) {
        this.debugSession = debugSession;
    }

    public int getSessionCount() {
        return this.globalSessionMap.size();
    }
}
