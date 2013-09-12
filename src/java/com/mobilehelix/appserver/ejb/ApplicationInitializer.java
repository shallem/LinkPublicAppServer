/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ejb;

import com.mobilehelix.appserver.connections.MHConnectException;
import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.session.CredentialsManager;
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
public class ApplicationInitializer {
    public Future<Integer> doInit(ApplicationFacade af, CredentialsManager credentials) 
            throws AppserverSystemException, MHConnectException {
        return new AsyncResult<>(af.doInitOnSessionCreate(credentials));
    }
}
