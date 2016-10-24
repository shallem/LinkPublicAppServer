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
package com.mobilehelix.appserver.ejb;

import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.session.CredentialsManager;
import com.mobilehelix.appserver.session.Session;
import com.mobilehelix.appserver.settings.ApplicationSettings;
import com.mobilehelix.services.objects.WSUserPreference;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Abstract base class that defines an interface to application-specific initialization.
 * This allows a Session object, upon creation, to easily initialize each application 
 * provisioned to that session. Initialization is done in parallel using the @Asynchronous 
 * ResourceInitializer class.
 * 
 * @author shallem
 */
public abstract class ApplicationFacade {
    private Future<Integer> initStatus;
    private Long appID;
    private boolean loadInitDone = false;
    
    /**
     * Is this resource inside of an application that is visible on the device?
     */
    private final boolean isVisibleOnDevice;
    
    public ApplicationFacade(ApplicationSettings settings) {
        this.isVisibleOnDevice = settings.isIsVisibleOnDevice();
    }
    
    public ApplicationFacade(boolean isVisibleOnDevice) {
        this.isVisibleOnDevice = isVisibleOnDevice;
    }

    /**
     * Get the result of the asynchronous initialization. Returns null if no async
     * initialization occurred. Otherwise either (a) returns the init status, or (b)
     * blocks waiting for the init to finish.
     * 
     * @return 
     * @throws java.lang.InterruptedException 
     * @throws java.util.concurrent.ExecutionException 
     */
    public Integer getInitStatus() throws InterruptedException, ExecutionException {
        if (initStatus == null) {
            return 0;
        }
        return initStatus.get();
    }

    public void setInitStatus(Future<Integer> initStatus) {
        this.initStatus = initStatus;
    } 

    public Long getAppID() {
        return appID;
    }

    public void setAppID(Long appID) {
        this.appID = appID;
    }

    public abstract int getAppType();
    
    /**
     * Abstract per-application init function to be executed when a session is 
     * first created.
     * 
     * @return
     * @throws AppserverSystemException 
     */
    public abstract Integer doInitOnSessionCreate(Session session, CredentialsManager credentials) 
            throws AppserverSystemException;
    
    /**
     * Abstract per-application init to be executed when an application is first loaded.
     * 
     * @param session
     * @param credentials
     * @return
     * @throws AppserverSystemException 
     */
    public abstract Integer doInitOnLoad(Session session,
            CredentialsManager credentials) throws AppserverSystemException;

    /**
     * Indicates that doInitOnLoad has been called.
     */
    public void setInitOnLoadDone() {
        this.loadInitDone = true;
    }
    
    /**
     * Return if doInitOnLoad has been called.
     * @return 
     */
    public boolean getInitOnLoadDone() {
        return this.loadInitDone;
    }

    public boolean isIsVisibleOnDevice() {
        return isVisibleOnDevice;
    }
    
    /**
     * Get a default preference value. If no default is available, return null.
     * 
     * @param tag Prefs tag.
     * @param sess Current session.
     * @return 
     * @throws java.io.IOException 
     */
    public WSUserPreference getDefaultPreference(String tag, Session sess) throws IOException {
        return null;
    }
    
    /**
     * Override-able close method, which is invoked when the session is deleted from
     * the session manager. This is the place to ensure that any open sockets,
     * files, etc. are cleaned up. By default, nothing happens here.
     */
    public void close() {
        
    }
}
