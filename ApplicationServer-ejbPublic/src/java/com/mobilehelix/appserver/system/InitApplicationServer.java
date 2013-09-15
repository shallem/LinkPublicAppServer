/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.system;

import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.session.GlobalPropertiesManager;
import com.mobilehelix.appserver.session.SessionManager;
import com.mobilehelix.services.objects.ApplicationServerInitRequest;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * Registers an application server with the Controller. 
 * 
 * @author shallem
 */
@Singleton
@Startup
@EJB(name="java:global/InitApplicationServer", beanInterface=InitApplicationServer.class)
public class InitApplicationServer {
    
    // Global properties
    @EJB
    private GlobalPropertiesManager globalProperties;
    
    // Global session manager
    @EJB
    private SessionManager sessionMgr;
    
    private boolean isInitialized = false;
    
    private ControllerConnectionBase controllerConnection;
    
    @PostConstruct
    public void init() {
        try {
            // See if we have a connection to the Controller.
            Class c = Class.forName("com.mobilehelix.appserver.system.ControllerConnection");
            Constructor cc = c.getDeclaredConstructor();
            this.controllerConnection = (ControllerConnectionBase)cc.newInstance(new Object[]{});
        } catch(ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            
        }
        
        if (controllerConnection == null) {
            controllerConnection = new ControllerConnectionBase();
        }
        this.controllerConnection.setSessionMgr(sessionMgr);
        this.controllerConnection.setGlobalProperties(globalProperties);
    }
    
    /**
     * Called to register the application server with the controller and to re-read
     * all Glassfish system properties. The parameter supplied is the keystore password
     * protecting the keystore containing this server's credentials, including private
     * key and certificate. This file must be generated by the installation procedure
     * and is referenced by the com.mobilehelix.certdir system property.
     * @param storePass 
     */
    public void processInitRequest(ApplicationServerInitRequest asir, String privIP) throws AppserverSystemException {
        /* Register with the Controller, if we have one. */
        this.controllerConnection.processInitRequest(asir, privIP);
        
        /* Indicate that the server is now initialized. */
        this.isInitialized = true;
        
        /* If debugging is on, clear out the debug session. This forces the debug
         * session to be re-created with the latest applications when the debug user
         * next contacts the app server.
         */
        if (globalProperties.isDebugOn()) {
            sessionMgr.setDebugSession(null);
            System.setProperty("jcifs.util.loglevel", "3");
        }
    }
    
    public ControllerConnectionBase getControllerConnection() {
        return this.controllerConnection;
    }
    
    public boolean isIsInitialized() {
        return isInitialized;
    }
    
    public boolean validateSessionID(String incomingSessID) {
        return this.controllerConnection.validateSessionID(incomingSessID);
    }
    
    public Long getServerID() {
        return this.controllerConnection.getServerID();
    }
}
