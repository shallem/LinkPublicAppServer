/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author shallem
 */
public class WSServerList {
    private List<WSServer> servers;
    
    public WSServerList() {
        this.servers = new LinkedList<>();
    }
    
    public WSServerList(List<WSServer> serverList) {
        this.servers = serverList;
    }

    public WSServer[] getServers() {
        WSServer[] ret = new WSServer[servers.size()];
        return servers.toArray(ret);
    }

    public void setServers(List<WSServer> servers) {
        this.servers = servers;
    }
    
    public void addServer(WSServer s) {
        this.servers.add(s);
    }
}
