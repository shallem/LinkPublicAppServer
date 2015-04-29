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

import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * Used to read global properties and, when used without a Controller, global app
 * configuration.
 * 
 * @author shallem
 */
@Startup
@Singleton
@PermitAll
public class GlobalPropertiesManager {
    /* Main client name. */
    private String clientName;
    
    /* Debugging properties. */
    private boolean debugOn;
    private String debugUser;
    private String debugPassword;
    
    /* Indicates that there is no gateway required. */
    private boolean isNoGateway;
    
    /* Public and private IP addresses of this server. Note that the public IP
     * is the "externally accessible" address that is, for example, used to create
     * a push subscription in MS Exchange. The private ip/port is used by the gateway
     * to contact this server.
     */
    private String asPubIP;
    private String asPrivIP;
    private Integer asPubPort;
    private Integer asPrivPort;
    private Integer asHttpPort;
    
    private String rootDir;
    private String scriptsDir;
    private String phantomJsBin;
            
    
    @PostConstruct
    public void init() {
        this.isNoGateway = false;
        this.readDebugProperties();
    }
    
    private void readDebugProperties() {
        String debugOnStr = System.getProperty("com.mobilehelix.debug");
        if (debugOnStr != null && debugOnStr.equals("true")) {
            this.debugOn = true;
            this.debugUser = System.getProperty("com.mobilehelix.debuguser");
            this.debugPassword = System.getProperty("com.mobilehelix.debugpassword");
        } else {
            this.debugOn = false;
            this.debugUser = null;       
            this.debugPassword = null;
        }
    }
    
    public boolean isDebugOn() {
        return debugOn;
    }

    public String getDebugUser() {
        return debugUser;
    }
    
    public String getDebugPassword() {
        return debugPassword;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getAsPubIP() {
        return asPubIP;
    }

    public void setAsPubIP(String asPubIP) {
        this.asPubIP = asPubIP;
    }

    public String getAsPrivIP() {
        return asPrivIP;
    }

    public void setAsPrivIP(String asPrivIP) {
        this.asPrivIP = asPrivIP;
    }

    public Integer getAsPubPort() {
        return asPubPort;
    }

    public void setAsPubPort(Integer asPubPort) {
        this.asPubPort = asPubPort;
    }

    public Integer getAsPrivPort() {
        return asPrivPort;
    }

    public void setAsPrivPort(Integer asPrivPort) {
        this.asPrivPort = asPrivPort;
    }

    public Integer getAsHttpPort() {
        return asHttpPort;
    }

    public void setAsHttpPort(Integer asHttpPort) {
        this.asHttpPort = asHttpPort;
    }

    public String getScriptsDir() {
        return this.scriptsDir;
    }

    public void setScriptsDir(String scriptDir) {
        this.scriptsDir = scriptDir;
    }

    public String getPhantomJsBin() {
        return phantomJsBin;
    }

    public void setPhantomJsBin(String phantomJsBin) {
        this.phantomJsBin = phantomJsBin;
    }

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public boolean isIsNoGateway() {
        return isNoGateway;
    }

    public void setIsNoGateway(boolean isNoGateway) {
        this.isNoGateway = isNoGateway;
    }
    
    public void asProperties(Map<String, Object> props) {
        if (props == null)
            return;
        
        props.put("asPubIP", this.asPubIP);
        props.put("asPrivIP", this.asPrivIP);
        props.put("asPubPort", this.asPubPort);
        props.put("asPrivPort", this.asPrivPort);
        props.put("asHttpPort", this.asHttpPort);
        props.put("scriptsDir", this.scriptsDir);
        props.put("phantomJsBin", this.phantomJsBin);
    }
}
