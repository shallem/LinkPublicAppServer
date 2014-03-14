/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.clients;

import com.mobilehelix.services.interfaces.RestClient;
import com.mobilehelix.services.objects.ApplicationServerInitRequest;
import com.mobilehelix.services.objects.GenericBsonResponse;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import java.io.IOException;

/**
 *
 * @author shallem
 */
public class ApplicationServerInitClient extends RestClient {
    
    // Parameters that we send to the app server service.
    private String controllerIP;
    private Integer controllerPort;
    private String asPubIP;
    private Integer asPubPort;
    private Integer asPrivPort;
    private Integer asHttpPort;
    private String clientName;
    private String serverName;
    private String storePass;
    private String debugPassword;
    private byte[] clientKeystore;
    private String appScriptsDir;
    private String phantomJsBin;

    
    public ApplicationServerInitClient(String asIP,
            String asPubIP,
            Integer asPort,
            Integer asHttpPort,
            String controllerIP,
            Integer controllerPort,
            Integer asPubPort,
            String clientName,
            String serverName,
            String storePass,
            byte[] clientKeystore,
            String debugPassword,
            HTTPSProperties props,
            String scriptsDir,
            String phantomjsBin) {
        super(asIP + ":" + asPort.toString(), "/ws/initas", props);
        this.asPubIP = asPubIP;
        this.asPubPort = asPubPort;
        this.asPrivPort = asPort;
        this.asHttpPort = asHttpPort;
        this.controllerIP = controllerIP;
        this.controllerPort = controllerPort;
        this.storePass = storePass;
        this.clientName = clientName;
        this.serverName = serverName;
        this.clientKeystore = clientKeystore;
        this.debugPassword = debugPassword;
        this.appScriptsDir = scriptsDir;
        this.phantomJsBin = phantomjsBin;
    }

    public GenericBsonResponse runAppserverInit() throws IOException {
        ApplicationServerInitRequest asir = 
                new ApplicationServerInitRequest(this.controllerIP, this.controllerPort,
                    this.asPubIP, this.asPubPort, this.asPrivPort, this.asHttpPort, 
                    this.clientName, this.serverName, this.storePass, 
                    this.clientKeystore, this.debugPassword, this.appScriptsDir, 
                    this.phantomJsBin);
        byte[] output = super.runPost(asir.toBson());
        if (output == null) {
            throw new IOException("Failed to execute service");
        }
        GenericBsonResponse resp = new GenericBsonResponse(output);
        return resp;
    }
}
