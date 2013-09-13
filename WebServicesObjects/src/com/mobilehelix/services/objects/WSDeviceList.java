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
public class WSDeviceList {
    private WSDevice[] devices;
    
    public WSDeviceList() {
        
    }
    
    public WSDeviceList(List<WSDevice> deviceList) {
        devices = new WSDevice[deviceList.size()];
        devices = deviceList.toArray(devices);
    }

    public WSDevice[] getDevices() {
        return devices;
    }

    public void setDevices(WSDevice[] devices) {
        this.devices = devices;
    }
}
