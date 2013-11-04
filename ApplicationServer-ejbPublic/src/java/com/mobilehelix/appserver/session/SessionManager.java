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
import com.mobilehelix.appserver.ejb.ApplicationInitializer;
import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.services.objects.ApplicationServerCreateSessionRequest;
import java.util.HashMap;
import java.util.List;
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

    private HashMap<String, Session> globalSessionMap;
    private Session debugSession;

    /* EJB to perform async init on application settings. */
    @EJB
    private ApplicationInitializer appInit;
    
    /* Global properties. */
    @EJB
    private GlobalPropertiesManager globalProperties;
    
    @PostConstruct
    public void init() {
        globalSessionMap = new HashMap<>();
        
        // Determine what type of app registry we have ...
    }

    public void addSession(ApplicationServerCreateSessionRequest sess)
            throws AppserverSystemException {
        String sessIDB64 = new String(Base64.encodeBase64(sess.getSessionKey()));
        Session appServerSession = new Session(sess, appInit);
        globalSessionMap.put(sessIDB64, appServerSession);
    }

    public void doSessionInit(Session sess, Long[] appIDs, Integer[] appGenIDs)
            throws AppserverSystemException {
        sess.doAppInit(appIDs, appGenIDs, appInit);
    }

    public void deleteSession(byte[] sessionKey) {
        String sessIDB64 = new String(Base64.encodeBase64(sessionKey));
        
        Session s = globalSessionMap.get(sessIDB64);
        if (s != null) {
            s.close();
            globalSessionMap.remove(sessIDB64);
        }
    }

    private String getSessIDFromRequest(HttpServletRequest req) {
        String sessIDB64 = req.getHeader(HTTPHeaderConstants.MH_SESSION_ID_HEADER);
        if ((sessIDB64 == null)  && (req.getCookies() != null)) {
            for (Cookie c : req.getCookies()) {
                if (HTTPHeaderConstants.MH_SESSION_ID_HEADER.equalsIgnoreCase(c.getName())) {
                    sessIDB64 = c.getValue();
                    break;
                }
            }
        }
        return sessIDB64;
    }

    public Session getSessionForRequest(HttpServletRequest req) throws AppserverSystemException {
        String sessIDB64 = this.getSessIDFromRequest(req);
        if (sessIDB64 == null) {
            if (this.isDebugOn()) {
                if (this.debugSession == null) {
                    this.debugSession = new Session(this.getDebugUser(),
                        this.getDebugPassword(),
                            true);
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
