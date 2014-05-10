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
package com.mobilehelix.appserver.push;

import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.system.GlobalPropertiesManager;
import com.mobilehelix.appserver.session.Session;
import com.mobilehelix.appserver.settings.ApplicationSettings;
import com.mobilehelix.appserver.system.ApplicationServerRegistry;
import com.mobilehelix.services.objects.ApplicationServerCreateSessionRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.commons.codec.binary.Hex;

/**
 * Responsible for creating push subscriptions and caching them.
 * 
 * @author shallem
 */
@Startup
@Singleton
@EJB(name="java:global/PushManager", beanInterface=PushManager.class)
public class PushManager {
    private static final Logger LOG = Logger.getLogger(PushManager.class.getName());
    
    
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<PushReceiver> > userPushMap;
    private ConcurrentHashMap<String, PushReceiver> idMap;
    
    /* EJB to perform async init on application settings. */
    @EJB
    private PushInitializer pushInit;
    
    @EJB
    private ApplicationServerRegistry appRegistry;
    
    @EJB
    private GlobalPropertiesManager globalProperties;
    
    private String asHostPlusPort;
    private SecureRandom srandom;
    
    @PostConstruct
    public void init() {
        userPushMap = new ConcurrentHashMap<>();
        idMap = new ConcurrentHashMap<>();
        srandom = new SecureRandom();
    }
    
    @PreDestroy
    public void unsubscribe() {
        for (PushReceiver r : idMap.values()) {
            r.unsubscribe();
        }
    }
    
    public void refresh(Session sess, Long appID) {
        String combinedUser = MessageFormat.format("{0}|{1}", new Object[]{ sess.getClient(), 
            sess.getCredentials().getUsername() });
        ConcurrentLinkedQueue<PushReceiver> receivers = this.userPushMap.get(combinedUser);
        if (receivers != null && !receivers.isEmpty()) {
            for (PushReceiver receiver : receivers) {
                if (receiver.matches(sess.getClient(), sess.getCredentials().getUsername(), appID)) {
                    receiver.refresh(sess.getCredentials().getUsername(), sess.getCredentials().getPassword());
                }
            }
        }
    }
    
    public void addSession(ApplicationServerCreateSessionRequest newSess) throws AppserverSystemException {
        Long[] appIDs = newSess.getAppIDs();
        Integer[] appGenIDs = newSess.getAppGenIDs();
        
        if (this.asHostPlusPort == null) {
            this.asHostPlusPort = globalProperties.getAsPubIP() + ":" + globalProperties.getAsHttpPort().toString();
        }
        
        for (int i = 0; i < appIDs.length; ++i) {
            Long appID = appIDs[i];
            Integer appGenID = appGenIDs[i];
            ApplicationSettings as = 
                    appRegistry.getSettingsForAppID(newSess.getClient(), appID, appGenID);
            if (as == null) {
                /* The registration does not tell us the app type. Hence we may get
                 * normal web apps in our ID list. We just need to skip these ...
                 */
                continue;
            }
            if (as.getPushReceiver() == null) {
                /**
                 * App does not support push.
                 */
                continue;
            }
            LOG.log(Level.FINE, "Create or refresh push session for app {0}", as.getAppName());
            
            // See if we have a push receiver for client/user/app
            boolean found = false;
            String combinedUser = MessageFormat.format("{0}|{1}", new Object[]{ newSess.getClient(), newSess.getUserID() });
            ConcurrentLinkedQueue<PushReceiver> receivers = this.userPushMap.get(combinedUser);
            if (receivers != null && !receivers.isEmpty()) {
                for (PushReceiver receiver : receivers) {
                    if (receiver.matches(newSess.getClient(), newSess.getUserID(), appID)) {
                        found = true;
                        LOG.log(Level.FINE, "Refreshing push session for {0}", combinedUser);
                        receiver.refresh(newSess.getUserID(), newSess.getPassword());
                    }
                }
            }
            try {
                if (!found) {
                    LOG.log(Level.FINE, "Creating push session for {0}", combinedUser);
                    String uniqueID = this.getUniqueID(newSess.getClient(), newSess.getUserID(), appID);
                    PushReceiver newReceiver = as.getPushReceiver();
                    PushCompletion pushAccepted = new PushCompletion(this.userPushMap, this.idMap, uniqueID, combinedUser, newReceiver);
                    pushInit.doInit(newReceiver, 
                            asHostPlusPort, 
                            uniqueID, 
                            newSess.getClient(), 
                            newSess.getUserID(), 
                            newSess.getPassword(), 
                            newSess.getDeviceType(), 
                            as,
                            pushAccepted);
                    /*
                    
                    if (newReceiver.create(asHostPlusPort, uniqueID, newSess.getClient(), newSess.getUserID(), newSess.getPassword(), newSess.getDeviceType(), as)) {
                        LOG.log(Level.FINE, "Created push session for {0}, ID {1}", new Object[] {
                            combinedUser,
                            uniqueID
                        });   
                    } */
                }
            } catch(NoSuchAlgorithmException | UnsupportedEncodingException ex) {
                LOG.log(Level.SEVERE, "Failed to create push session.", ex);
                throw new AppserverSystemException("Failed to create push session.", 
                        "FailedToCreatePushSession",
                        new String[] { ex.getMessage() });
            }
        } 
    }
    
    private String getUniqueID(String clientid,
            String userid,
            Long appID) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.reset();
        digest.update(clientid.getBytes("utf8"));
        digest.update(userid.getBytes("utf8"));
        digest.update(appID.toString().getBytes("utf8"));
        
        // Add in a random 8 bytes salt.
        byte saltb[] = new byte[8];
        srandom.nextBytes(saltb);
        digest.update(saltb);
        
        byte[] res = digest.digest();
        return new String(Hex.encodeHex(res, true));
    }
    
    public PushReceiver getPushReceiver(String uniqueID) {
        return idMap.get(uniqueID);
    }
    
    public int getPushSessionCount() {
        return idMap.keySet().size();
    }
}
