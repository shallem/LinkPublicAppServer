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
package com.mobilehelix.appserver.system;

import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.session.SessionManager;
import com.mobilehelix.services.objects.ApplicationServerInitRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private static final Logger LOG = Logger.getLogger(InitApplicationServer.class.getName());
    
    // Global properties
    @EJB
    private GlobalPropertiesManager globalProperties;
    
    // Global session manager
    @EJB
    private SessionManager sessionMgr;

    // Global version manager.
    @EJB
    private VersionManager versionMgr;
    
    @EJB
    private ApplicationServerRegistry appRegistry;
    
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
        this.controllerConnection.setApplicationRegistry(this.appRegistry);

        // See if we can init from helix-init.properties in the instance root.
        // Check for the init properties file in the domain.
        try {
            String domainDir = System.getProperty("com.sun.aas.instanceRoot");
            File initPropertiesFile = new File(domainDir + File.separator + "helix-init.properties");
            if (initPropertiesFile.exists()) {
                Properties initProperties = new Properties();
                FileInputStream initStream = new FileInputStream(initPropertiesFile);
                try {
                    initProperties.load(initStream);
                } finally {
                    initStream.close();
                }
                ApplicationServerInitRequest asir = new ApplicationServerInitRequest();
                asir.setAsPrivIP(initProperties.getProperty("PrivateIP"));
                asir.setAsPubIP(initProperties.getProperty("PublicIP"));
                asir.setAsPubPort(Integer.parseInt(initProperties.getProperty("HttpsPort")));
                asir.setAsPrivPort(Integer.parseInt(initProperties.getProperty("HttpsPort")));
                asir.setAsHttpPort(Integer.parseInt(initProperties.getProperty("HttpPort")));
                asir.setControllerIP(initProperties.getProperty("ControllerIP"));
                asir.setControllerPort(Integer.parseInt(initProperties.getProperty("ControllerHttpsPort")));
                asir.setClientName(initProperties.getProperty("Client"));
                asir.setServerName(initProperties.getProperty("ServerName"));
                asir.setPushServerName(asir.getServerName() + "-PUSH");
                asir.setStorePass(initProperties.getProperty("StorePass"));
                asir.setIsNoGateway(Boolean.parseBoolean(initProperties.getProperty("NoGateway")));
                asir.setRootDir(initProperties.getProperty("RootDir"));
                asir.setScriptsDir(initProperties.getProperty("ScriptsDir"));
                asir.setPhantomJsBin(initProperties.getProperty("PhantomJSBinDir"));
                asir.setRegionName(initProperties.getProperty("ServerRegion"));
                
                // Read in the keystore from the provided path.
                String ksPath = initProperties.getProperty("CertDir");
                // Make sure we have the credentials we need.
                File jksPath = new File(ksPath + File.separator + "keystore.jks");
                if (!jksPath.exists()) {
                    throw new IOException("The server credentials path " + jksPath.getAbsolutePath() + " does not exist.");
                }

                // Validate that the jks file has a reasonable length ...
                long length = jksPath.length();
                if (length > Integer.MAX_VALUE) {
                    // File is too large
                    throw new IOException("JKS file is corrupt or cert path is wrong. keystore.jks has more than Integer.MAX_VALUE bytes: "+length);
                }

                // Read in the raw bytes from the keystore.
                try (FileInputStream jksInstream = new FileInputStream(jksPath)) {
                    byte[] jksBytes = new byte[(int)length];
                    int nread = jksInstream.read(jksBytes);
                    if (nread < length) {
                        throw new IOException("Failed to read full JKS file.");
                    }
                    asir.setKeyStore(jksBytes);
                }
                
                // Retry 5 times in case the Controller has not yet started/initialized.
                this.processInitRequest(asir, 5);
            }
        } catch(IOException ioe) {
            LOG.log(Level.SEVERE, "Initialization from helix-init.properties failed.", ioe);
        } catch(AppserverSystemException ae) {
            LOG.log(Level.SEVERE, ae.getLocalizedMessage(), ae);
        }
    }
    
    /**
     * Called to register the application server with the controller and to re-read
     * all Glassfish system properties. The parameter supplied is the keystore password
     * protecting the keystore containing this server's credentials, including private
     * key and certificate. This file must be generated by the installation procedure
     * and is referenced by the com.mobilehelix.certdir system property.
     * @param asir 
     * @param nRetries the number of times to retry the init request to the Controller. 
     *  Allows us to avoid having to sequence services when the system restarts.
     * @return  
     * @throws com.mobilehelix.appserver.errorhandling.AppserverSystemException 
     */
    public String processInitRequest(ApplicationServerInitRequest asir, int nRetries) throws AppserverSystemException {
        LOG.log(Level.FINE, "Received init request with parameters: PublicIP={0},PubPort={1},PrivateIP={2},PrivPort={3}",
                new Object[]{
                    asir.getAsPubIP(),
                    asir.getAsPubPort().toString(),
                    asir.getAsPrivIP(),
                    asir.getAsPrivPort().toString()
                });
        
        /* Register with the Controller, if we have one. */
        String ret = this.controllerConnection.processInitRequest(asir, versionMgr.getVersion(), nRetries);
        appRegistry.setControllerConnection(this.controllerConnection);
        
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
        LOG.log(Level.INFO, "Application server initialization completed successfully.");
        return ret;
    }
    
    public ControllerConnectionBase getControllerConnection() {
        return this.controllerConnection;
    }
    
    public boolean isIsInitialized() {
        return this.isInitialized;
    }
    
    public boolean validateSessionID(String incomingSessID) {
        return this.controllerConnection.validateSessionID(incomingSessID);
    }
    
    public Long getServerID() {
        return this.controllerConnection.getServerID();
    }
    
    public Long getPushServerID() {
        return this.controllerConnection.getPushServerID();
    }
           
    public String getPushServerName() {
        return this.controllerConnection.getPushServerName();
    }
}
