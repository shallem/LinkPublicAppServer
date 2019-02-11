/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ws;

import org.glassfish.jersey.server.ResourceConfig;

public class CustomResourceConfig extends ResourceConfig {
    public CustomResourceConfig() {
        packages(CustomResourceConfig.class.getPackage().getName());
    }
}