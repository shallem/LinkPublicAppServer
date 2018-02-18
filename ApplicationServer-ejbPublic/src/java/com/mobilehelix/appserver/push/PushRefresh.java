/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.push;

import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author v3devel
 */
public interface PushRefresh {
    public byte[] doRefresh(MultivaluedMap<String, String> params, PushManager mgr) throws AppserverSystemException;
}
