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
import com.mobilehelix.services.objects.WSUserPreference;
import java.util.Collection;
import java.util.concurrent.Future;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

/**
 *
 * @author shallem
 */
@Stateless
@Asynchronous
public class PushInitializer {
    public Future<Boolean> doInit(PushReceiver pr, 
            String appServerHostName,
            String uniqueID, 
            String combinedUser,
            String clientid,
            String userid,
            String password,
            String deviceType,
            Long appID,
            ApplicationSettings appSettings,
            PushCompletion onComplete,
            Collection<WSUserPreference> settings,
            PushManager pushMgr) 
            throws AppserverSystemException {
        return new AsyncResult<>(pr.doCreate(appServerHostName, uniqueID, combinedUser, clientid, userid, password, deviceType, appID, appSettings, onComplete, settings, pushMgr));
    }
    
    public Future<Boolean> doRefresh(PushReceiver pr,
            String userid,
            String password,
            ApplicationSettings appSettings,
            Collection<WSUserPreference> settings) throws AppserverSystemException {
        pr.refresh(userid, password, appSettings, false, settings);
        return new AsyncResult<>(true);
    }
}
