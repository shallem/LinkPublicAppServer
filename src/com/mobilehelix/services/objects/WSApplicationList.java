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
public class WSApplicationList {
    private WSApplication[] apps;
    
    public WSApplicationList() {
        
    }
    
    public WSApplicationList(List<WSApplication> appList) {
        this.apps = new WSApplication[appList.size()];
        this.apps = appList.toArray(this.apps);
    }

    public WSApplication[] getApps() {
        return apps;
    }

    public void setApps(WSApplication[] apps) {
        this.apps = apps;
    }
}
