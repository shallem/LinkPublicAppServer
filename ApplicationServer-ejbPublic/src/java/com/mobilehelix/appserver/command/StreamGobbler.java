/*
 * See http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
 */
package com.mobilehelix.appserver.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shallem
 */
public class StreamGobbler {
    private static final Logger LOG = Logger.getLogger(StreamGobbler.class.getName());
    
    private InputStream is;
    private String type;
    private StringBuilder output;
    private String doneMsg;
    
    public StreamGobbler(InputStream is, String type, String doneMsg)
    {
        this.is = is;
        this.type = type;
        this.output = new StringBuilder();
        this.doneMsg = doneMsg;
    }
    
    public String getOutput() {
        return output.toString();
    }
    
    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null) {
                LOG.log(Level.SEVERE, "{0}> {1}", new Object[] {
                    this.type,
                    line
                });
                output.append(line);
            }
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "Failed to capture command output", ioe);
        }
    }
    
    public void appendDoneMessage() {
        if (this.doneMsg != null) {
            output.append(this.type).append("> ").append(this.doneMsg).append("\n");
        }
    }
}
