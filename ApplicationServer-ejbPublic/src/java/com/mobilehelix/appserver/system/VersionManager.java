/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.system;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 *
 * @author shallem
 */
@Singleton
@Startup
public class VersionManager {
    private static final Logger LOG = Logger.getLogger(VersionManager.class.getName());
    
    static private final String versionProperties =
            "/com/mobilehelix/version/version.properties";
    private boolean initDone = false;
    
    private String majorVer = "";
    private String minorVer = "";
    private String revisionVer = "";
    
    private String versionString = "1.0.0";
    
    @PostConstruct
    public void init() {
        if (initDone) {
            return;
        }
        try {
            this.readVersion();
            initDone = true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    private void readVersion() throws Exception {
        InputStream verStream = 
                getClass().getClassLoader().getResourceAsStream(versionProperties);
        Properties verProps = new Properties();
        verProps.load(verStream);
            
        majorVer = verProps.getProperty("Major");
        minorVer = verProps.getProperty("Minor");
        revisionVer = verProps.getProperty("Revision");
    
        versionString = majorVer + "." + minorVer + "." + revisionVer;
    }
    
    public String getVersion() {
        return versionString;
    }
}
