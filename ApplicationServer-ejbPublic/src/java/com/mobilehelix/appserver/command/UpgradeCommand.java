/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.command;

import com.mobilehelix.appserver.system.GlobalPropertiesManager;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

/**
 *
 * @author shallem
 */
@Singleton
public class UpgradeCommand  {
    private static final Logger LOG = Logger.getLogger(UpgradeCommand.class.getName());
  
    @EJB
    private GlobalPropertiesManager globalProps;
    
    private String scriptExtension;
    private String shellCmd;
    
    @PostConstruct
    public void init() {
        if (SystemUtils.IS_OS_WINDOWS) {
            scriptExtension = ".bat";
            shellCmd = "cmd /c";
        } else {
            scriptExtension = ".sh";
            shellCmd = "bash";
        }
    }
    
    public String run() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(this.shellCmd, globalProps.getRootDir() + File.separator + "autoupgrade" + this.scriptExtension);
        pb.directory(new File(globalProps.getRootDir()));
        
        File outputFile = new File(globalProps.getRootDir() + File.separator + "LOG.out");
        pb.redirectOutput(outputFile);
        File errorFile = new File(globalProps.getRootDir() + File.separator + "LOG.err");
        pb.redirectError(errorFile);
        
        LOG.log(Level.SEVERE, "The appserver is about to autoupgrade.");
        Process proc = pb.start();
        
        try {
            StringBuilder sb = new StringBuilder();
            if (proc.exitValue() != 0) {
                sb.append(FileUtils.readFileToString(outputFile));
                sb.append(FileUtils.readFileToString(errorFile));
                return sb.toString();
            } else {
                sb.append(FileUtils.readFileToString(outputFile));
                return sb.toString();                
            }
        } catch(Exception e) {
            // Ignore - that means the process is still running, which is good.
        }
        return null;
    }
}
