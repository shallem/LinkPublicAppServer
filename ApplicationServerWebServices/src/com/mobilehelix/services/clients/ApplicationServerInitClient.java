package com.mobilehelix.services.clients;

import com.mobilehelix.services.interfaces.ApacheClientInterface;
import com.mobilehelix.services.interfaces.ApacheRestClient;
import com.mobilehelix.services.objects.ApplicationServerInitRequest;
import com.mobilehelix.services.objects.GenericBsonResponse;
import java.io.IOException;

/**
 *
 * @author shallem
 */
public class ApplicationServerInitClient extends ApacheClientInterface {
    
    // Parameters that we send to the app server service.
    private final String controllerIP;
    private final Integer controllerPort;
    private final String asPrivIP;
    private final String asPubIP;
    private final Integer asPubPort;
    private final Integer asPrivPort;
    private final Integer asHttpPort;
    private final String clientName;
    private final String serverName;
    private final String storePass;
    private final String debugPassword;
    private final byte[] clientKeystore;
    private final String appScriptsDir;
    private final String rootDir;
    private boolean isNoGateway;
    
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
            ApacheRestClient cli,
            String scriptsDir,
            String rootDir,
            boolean isNoGateway) {
        super(cli, asIP + ":" + asPort.toString(), "/ws/initas", 3);
        this.asPrivIP = asIP;
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
        this.rootDir = rootDir;
        this.isNoGateway = isNoGateway;
    }

    public GenericBsonResponse runAppserverInit() throws IOException {
        ApplicationServerInitRequest asir = 
                new ApplicationServerInitRequest(this.controllerIP, this.controllerPort,
                    this.asPrivIP, this.asPubIP, this.asPubPort, this.asPrivPort, this.asHttpPort, 
                    this.clientName, this.serverName, this.storePass, 
                    this.clientKeystore, this.debugPassword, this.appScriptsDir, 
                    "", this.rootDir, this.isNoGateway);
        byte[] output = this.getClient().bsonPost(this.getURL(), asir.toBson(), this.getNtries());
        if (output == null) {
            throw new IOException("Failed to execute service");
        }
        GenericBsonResponse resp = new GenericBsonResponse(output);
        return resp;
    }
}
