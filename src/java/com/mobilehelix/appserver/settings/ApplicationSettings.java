/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.settings;

import com.mobilehelix.appserver.ejb.ApplicationFacade;
import com.mobilehelix.appserver.session.CredentialsManager;
import com.mobilehelix.appserver.system.ApplicationServerRegistry;
import com.mobilehelix.wsclient.common.Applications.WSApplication;
import com.mobilehelix.wsclient.common.Extras.WSExtra;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author shallem
 */
public abstract class ApplicationSettings<T> {
    private Long appID;
    private String appName;
    private String appURL;
    private Integer appGenID;
    private Integer appType;
    
    public ApplicationSettings(WSApplication app) {
        this.appID = app.getUniqueID();
        this.appName = app.getAppName();
        this.appGenID = app.getAppGenID();
        this.appType = app.getAppType();

        for (WSExtra wse : app.getAppExtras()) {
            switch (wse.getTag()) {
                case "email_server":
                case "jira_server":
                case "sharepoint_server":
                    this.appURL = wse.getValue();
                    break;
            }
        }
    }

    public Long getAppID() {
        return appID;
    }

    public String getAppURL() {
        return appURL;
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
    
    protected List<String> parseStringList(String val) {
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
     * @return 
     */
    public abstract ApplicationFacade createFacade(ApplicationServerRegistry appRegistry,
            boolean debugOn);
}
