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
package com.mobilehelix.appserver.session;

/**
 *
 * @author shallem
 */
public class CredentialsManager {
    // Credentials that were used to initiate the device session.
    private String username;
    private String password;
    private String client;
    
    // Per-request credentials that were (potentially) sent along with the
    // request to the device.
    private String requestUsername;
    private String requestPassword;
    
    public CredentialsManager(String client, String username, String password) {
        this.client = client;
        this.username = username;
        this.password = password;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }
    
    public String getPassword() {
        if (this.requestPassword != null) {
            return this.requestPassword;
        }
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        if (this.requestUsername != null) {
            return this.requestUsername;
        }
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getRequestUsername() {
        return requestUsername;
    }

    public boolean setRequestUsername(String newRequestUsername) {
        if (!newRequestUsername.equals(this.requestUsername)) {
            this.requestUsername = newRequestUsername;
            return true;
        }
        return false;
    }

    public String getRequestPassword() {
        return requestPassword;
    }

    public boolean setRequestPassword(String newRequestPassword) {
        if (!newRequestPassword.equals(this.requestPassword)) {
            this.requestPassword = newRequestPassword;
            return true;
        }
        return false;
    }

    public String getUsernameForSharepoint() {
        // Converts a username that we specify as user@domain to the sharepoint form:
        // test.mobilehelix.com\\Seth.
        String outUsername = this.getUsername();
        int atIdx = outUsername.indexOf("@");
        if (atIdx > 0) {
            String domainUser = outUsername.substring(0, atIdx);
            return domainUser;
/*            outUsername = MessageFormat.format("{0}\\{1}", 
                    new Object[]{ domainStr, domainUser }); */
        }
        return outUsername;
    }
    
    public String getDomainForSharepoint() {
        // Converts a username that we specify as user@domain to the sharepoint form:
        // test.mobilehelix.com\\Seth.
        String outUsername = this.getUsername();
        int atIdx = outUsername.indexOf("@");
        if (atIdx > 0) {
            String domainStr = outUsername.substring(atIdx + 1);
            return domainStr;
        }
        return null;
    }
}
