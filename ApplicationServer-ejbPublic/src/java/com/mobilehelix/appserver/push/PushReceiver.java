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
public interface PushReceiver {
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
    public boolean create(String appServerHostName,
            String uniqueID, 
            String clientid,
            String userid,
            String password,
            String deviceType,
            ApplicationSettings appSettings) throws AppserverSystemException;
    
    /**
     * Called each time the user creates a new session on this server. On each session
     * create the user's credentials may have changed. Any other relevant state should
     * be updated here.
     * 
     * @param userid
     * @param password 
     */
    public void refresh(String userid,
            String password);
    
    /**
     * Called when a POST request to the push servlet using the unique ID of this object (specified
     * in create) is received.
     * 
     * @param input Raw input data supplied as the post body.
     * @return Return the data to send in response to the incoming https request.
     */
    public byte[] receive(byte[] headers, byte[] body);

    /**
     * Called to determine if a receiver matches the unique combination of client, user, and app ID.
     * Should return true if it does.
     */
    public boolean matches(String client,
            String userid,
            Long appID);
}
