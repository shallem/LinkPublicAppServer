/*
 * Copyright 2013 Mobile Helix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mobilehelix.appserver.settings;

import com.mobilehelix.appserver.ejb.ApplicationFacade;
import com.mobilehelix.appserver.push.PushReceiver;
import com.mobilehelix.appserver.session.Session;
import com.mobilehelix.appserver.system.ApplicationServerRegistry;
import com.mobilehelix.services.objects.WSApplication;
import com.mobilehelix.services.objects.WSExtra;
import com.mobilehelix.services.objects.WSExtraGroup;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 *
 * @author shallem
 * @param <T>
 */
public abstract class ApplicationSettings<T> {
    private final String client;
    private final Long appID;
    private final String appName;
    private final Integer appGenID;
    private final Integer appType;
    private boolean isVisibleOnDevice;
    
    public ApplicationSettings(String client, WSApplication app) {
        this.client = client;
        this.appID = app.getUniqueID();
        this.appName = app.getAppName();
        this.appGenID = app.getAppGenID();
        this.appType = app.getAppType();
        this.isVisibleOnDevice = true;
        for (WSExtraGroup wseg : app.getAppExtraGroups()) {
            for (WSExtra wse : wseg.getExtras()) {
                if (wse.getTag().equals("web_app_to_device")) {
                    this.isVisibleOnDevice = wse.getValueBoolean();
                }
            }
        }
    }

    public ApplicationSettings(String client, long appID, String appName, int appGenID, int appType)  {
        this.client = client;
        this.appID = appID;
        this.appName = appName;
        this.appGenID = appGenID;
        this.appType = appType;
    }

    public String getClient() {
        return client;
    }
    
    public Long getAppID() {
        return appID;
    }

    public String getAppName() {
        return appName;
    }

    public Integer getAppGenID() {
        return appGenID;
    }

    public Integer getAppType() {
        return appType;
    }

    public boolean isIsVisibleOnDevice() {
        return isVisibleOnDevice;
    }
    
    protected static List<String> parseStringList(String val) {
        if (val.isEmpty()) {
            return Arrays.asList(new String[]{});
        }

        // Should be a newline-separated list.
        String[] names = val.split("[\\n]");
        for (int i = 0; i < names.length; ++i) {
            names[i] = names[i].trim();
        }
        return Arrays.asList(names);
    }
    
    public abstract T getExtrasSettings();
    
    public abstract String getContextPath();
    
    /**
     * Creates an application-specific facade using the values in this settings 
     * object.
     * @param sess
     * @param appRegistry
     * @param debugOn
     * @return 
     */
    public abstract ApplicationFacade createFacade(Session sess,
            ApplicationServerRegistry appRegistry,
            boolean debugOn);
    
    /**
     * Returns a push receiver object, if relevant for this app type. By default apps
     * have no push support so we return null.
     * @return 
     */
    public PushReceiver getPushReceiver() {
        return null;
    }
    
    /**
     * Ping the server's handling of this application. If there are dependent servers (e.g.,
     * the WorkSite agent ...), make sure we can still reach them.
     * 
     * @param warningMsgs
     * @param completedPings
     * @return 
     */
    public boolean doPing(List<String> warningMsgs, Set<String> completedPings) {
        // By default, return true (this app is still working), and do not add any warning messages
        return true;
    }
}
