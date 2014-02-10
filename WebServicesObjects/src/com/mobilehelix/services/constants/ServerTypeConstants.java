/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.constants;

/**
 *
 * @author shallem
 */
public class ServerTypeConstants {
    /* Constant indicating that a particular request is from a device, not another server. */
    public static final int SERVER_TYPE_DEVICE = -1;

    /* Contants corresponding to different types of servers. */
    public static final int SERVER_TYPE_GATEWAY = 1;
    public static final int SERVER_TYPE_APPLICATION_SERVER = 2;
    public static final int SERVER_TYPE_MONGODB_SERVER = 3;
    public static final int SERVER_TYPE_POSTGRES_SERVER = 4;
    public static final int SERVER_TYPE_PUSH_SERVER = 5;
    public static final int SERVER_TYPE_ROUTER_SERVER = 6;
    public static final int SERVER_TYPE_JIRA_SERVER = 7;
    public static final int SERVER_TYPE_CUSTOM = 255;
}
