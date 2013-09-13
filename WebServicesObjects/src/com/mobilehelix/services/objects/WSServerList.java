/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import java.util.List;

/**
 *
 * @author shallem
 */
public class WSServerList {
    private WSServer[] servers;
    
    public WSServerList() {
        
    }
    
    public WSServerList(List<WSServer> serverList) {
        this.servers = new WSServer[serverList.size()];
        this.servers = serverList.toArray(this.servers);
    }

    public WSServer[] getServers() {
        return servers;
    }

    public void setServers(WSServer[] servers) {
        this.servers = servers;
    }
}
