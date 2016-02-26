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
import java.util.concurrent.Callable;

/**
 *
 * @author shallem
 */
public class ApplicationInitializer implements Callable<Integer> {
    private final ApplicationFacade af;
    private final Session session;
    private final CredentialsManager credentials;
    
    public ApplicationInitializer(ApplicationFacade af, Session session, CredentialsManager credentials) {
        this.af = af;
        this.session = session;
        this.credentials = credentials;
    }
    
    @Override
    public Integer call() 
            throws AppserverSystemException {
        return af.doInitOnSessionCreate(session, credentials);
    }
}
