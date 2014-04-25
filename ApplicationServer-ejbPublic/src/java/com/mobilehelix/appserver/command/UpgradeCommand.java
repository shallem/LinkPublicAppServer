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
public class UpgradeCommand  {
    private static final Logger LOG = Logger.getLogger(UpgradeCommand.class.getName());
  
    @EJB
    private GlobalPropertiesManager globalProps;
    
    public String run() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("bash", globalProps.getRootDir() + File.separator + "autoupgrade.sh");
        pb.directory(new File(globalProps.getRootDir()));
        pb.redirectOutput(new File(globalProps.getRootDir() + File.separator + "LOG.out"));
        pb.redirectError(new File(globalProps.getRootDir() + File.separator + "LOG.err"));
        LOG.log(Level.SEVERE, "The appserver is about to autoupgrade.");
        Process proc = pb.start();
        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR", null);
        StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT", null);
        
        errorGobbler.start();
        outputGobbler.start();
        
        try {
            StringBuilder sb = new StringBuilder();
            if (proc.exitValue() != 0) {
                sb.append(outputGobbler.getOutput());
                sb.append(errorGobbler.getOutput());
                return sb.toString();
            } else {
                sb.append(outputGobbler.getOutput());
                return sb.toString();                
            }
        } catch(Exception e) {
            // Ignore - that means the process is still running, which is good.
        }
        return null;
    }
}
