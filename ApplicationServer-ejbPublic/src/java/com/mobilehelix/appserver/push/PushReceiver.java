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
import com.mobilehelix.appserver.settings.ApplicationSettings;

/**
 * Abstract interface for a client object that can handle push notification events.
 * In general, push notifications can be originated by the push server, which
 * accepts incoming notification requests to stylized URLs, or by any other piece
 * of the code. Each push receiver is intended to be a persistent, single user, single app
 * encapsulation of the current push state. When a push event is received, it should
 * be routed to the right receiver based on unique details of the push event that
 * reveal the client and user. The push receiver is then responsible for acting
 * on the push event, including sending the push notification via the Controller, which
 * provides a web service for sending out push events.
 * 
 * @author shallem
 */
public abstract class PushReceiver {
    private String uniqueID;
    private String clientid;
    private String userid;
    private String password;
    private String deviceType;
    private String combinedUser;
    private Long appID;
    
    /**
     * Invoked in the async init from the push manager. This method is responsible
     * for calling the override-able create method.
     */
    public final boolean doCreate(String appServerHostName,
            String uniqueID, 
            String combinedUser,
            String clientid,
            String userid,
            String password,
            String deviceType,
            Long appID,
            ApplicationSettings appSettings,
            PushCompletion pushAccepted) throws AppserverSystemException {
        this.uniqueID = uniqueID;
        this.combinedUser = combinedUser;
        this.clientid = clientid;
        this.userid = userid;
        this.password = password;
        this.deviceType = deviceType;
        this.appID = appID;
        
        return this.create(appServerHostName, uniqueID, clientid, userid, password, deviceType, appSettings, pushAccepted);
    }
    
    /**
     * Create the push subscription to the origin data source (e.g., Exchange). This
     * routine is only called if an existing push receiver DOES NOT exist on this 
     * server for this combination of clientid, userid, and app.
     * 
     * @param appServerHostName
     * @param uniqueID A unique ID that is by the push servlet (hosted at /push on this server) to find this receiver.
     * @param userid User ID.
     * @param password User password.
     * @param deviceType String identifying the type of this device.
     * @param appSettings Settings for the app requesting push support.
     * @return True if this app is now waiting for push notifications; false if not.
     * @throws AppserverSystemException 
     */
    public abstract boolean create(String appServerHostName,
            String uniqueID, 
            String clientid,
            String userid,
            String password,
            String deviceType,
            ApplicationSettings appSettings,
            PushCompletion pushAccepted) throws AppserverSystemException;
    
    /**
     * Called each time the user creates a new session on this server. On each session
     * create the user's credentials may have changed. Any other relevant state should
     * be updated here as well, including the user's policy.
     * 
     * @param userid
     * @param password 
     * @param appSettings 
     * @param isSessionCreate 
     * @param passwordVaultUserID 
     * @param passwordVaultPassword 
     */
    public abstract void refresh(String userid,
            String password,
            ApplicationSettings appSettings,
            boolean isSessionCreate,
            String passwordVaultUserID,
            String passwordVaultPassword);
    
    /**
     * Called by other components of the app server to let the push session know that the
     * user has refreshed data from the server. The idea is that counters or other state
     * should be reset after a user refreshes the data that he/she is being notified of. For 
     * example, this will reset the badge counts each time a user logs into the Link app and
     * accesses his or her email.
     */
    public abstract void refresh();
    
    /**
     * Called every 10 minutes. Gives the push session the opportunity to check to see if it is
     * still alive, if such a mechanism exists. This method should return true if the push session
     * is still valid and false if not. On false, the push manager will delete the push session.
     * @return 
     */
    public abstract boolean check();
    
    /**
     * Called when a POST request to the push servlet using the unique ID of this object (specified
     * in create) is received.
     * 
     * @param input Raw input data supplied as the post body.
     * @return Return the data to send in response to the incoming https request.
     */
    public abstract byte[] receive(byte[] input);

    /**
     * Called to determine if a receiver matches the unique combination of client, user, and app ID.
     * Should return true if it does.
     */
    public abstract boolean matches(String client,
            String userid,
            Long appID);
    
    /**
     * Called prior to shutdown to terminate this subscription.
     */
    public abstract void unsubscribe();
    
    /**
     * Return the unique ID stored in this push receiver.
     */
    public final String getUniqueID() {
        return this.uniqueID;
    }
    
    /**
     * Return the combined user/client that represents the identify of the owner of
     * this receiver.
     * 
     * @return 
     */
    public final String getCombinedUser() {
        return this.combinedUser;
    }

    public final String getClientid() {
        return clientid;
    }

    public final String getUserid() {
        return userid;
    }

    public final String getPassword() {
        return password;
    }

    public final String getDeviceType() {
        return deviceType;
    }

    public Long getAppID() {
        return appID;
    }
}