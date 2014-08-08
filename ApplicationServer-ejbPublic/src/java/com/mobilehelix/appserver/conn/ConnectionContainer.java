/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.conn;

/**
 *
 * @author shallem
 */
public interface ConnectionContainer {

    /**
     * Called when a session is complete to close all open connections.
     */
    public void close();
}
