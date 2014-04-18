/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.command;

import com.mobilehelix.appserver.session.GlobalPropertiesManager;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Singleton;

/**
 *
 * @author shallem
 */
@Singleton
public class UpgradeCommand {
    private static final Logger LOG = Logger.getLogger(UpgradeCommand.class.getName());
  
    @EJB
    private GlobalPropertiesManager globalProps;
    
    public void run() throws IOException {
        
        StringBuilder cmd = new StringBuilder();
        cmd.append("bash ").append(globalProps.getRootDir()).append(File.separator).append("autoupgrade.sh");
        Runtime rt = Runtime.getRuntime();
        LOG.log(Level.SEVERE, "The appserver is about to autoupgrade.");
        Process proc = rt.exec(cmd.toString());
        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR", null);
        StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT", null);

        errorGobbler.run();
        outputGobbler.run();
    }
}
