/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public final class WSUser {
    private String userID;
    private String firstName;
    private String lastName;
    private String email;
    private String tz;
    private String newUserPassword;
    private List<WSRole> userRoles;
    
    /**
     * Only supplied in WSUser objects returned from the Controller.
     */
    private WSPolicy computedPolicy;
    
    
    public WSUser() {
        
    }
    
    /**
     * Used to create a new user. When we create a new user we don't specify a policy. The
     * policy is computed based on the user's roles.
     * @param client
     * @param userID
     * @param firstName
     * @param lastName
     * @param email
     * @param tz
     * @param userRoles 
     */
    public WSUser(String userID,
            String firstName,
            String lastName,
            String email,
            String tz,
            List<WSRole> userRoles) {
        this.init(userID, firstName, lastName, email, tz, null, userRoles);
    }
    
    public WSUser(String userID,
            String firstName,
            String lastName,
            String email,
            String tz,
            WSPolicy wsp,
            List<WSRole> userRoles) {
        this.init(userID, firstName, lastName, email, tz, wsp, userRoles);
    }
    
    public void init(String userID,
            String firstName,
            String lastName,
            String email,
            String tz,
            WSPolicy wsp,
            List<WSRole> userRoles) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.tz = tz;
        this.computedPolicy = wsp;
        this.userRoles = userRoles;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTz() {
        return tz;
    }

    public void setTz(String TZ) {
        this.tz = TZ;
    }

    public String getNewUserPassword() {
        return newUserPassword;
    }

    public void setNewUserPassword(String newUserPassword) {
        this.newUserPassword = newUserPassword;
    }

    public WSPolicy getComputedPolicy() {
        return computedPolicy;
    }

    public void setComputedPolicy(WSPolicy computedPolicy) {
        this.computedPolicy = computedPolicy;
    }

    public void setUserRoles(List<WSRole> userRoles) {
        this.userRoles = userRoles;
    }
    
    public List<WSRole> getUserRoles() {
        return userRoles;
    }
    
    public void toBson(JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("userid");
        gen.writeString(this.userID);
        gen.writeFieldName("firstname");
        gen.writeString(this.firstName);
        gen.writeFieldName("lastname");
        gen.writeString(this.lastName);
        gen.writeFieldName("email");
        gen.writeString(this.email);
        if (this.newUserPassword != null) {
            gen.writeStringField("newpass", this.newUserPassword);
        }
        gen.writeArrayFieldStart("roles");
        for (WSRole role : this.userRoles) {
            role.toBson(gen);
        }
        gen.writeEndArray();
        if (this.computedPolicy != null) {
            gen.writeFieldName("policy");
            this.computedPolicy.toBson(gen, WSExtra.SerializeOptions.INCLUDE_ALL);
        }
        gen.writeFieldName("tz");
        gen.writeString(this.tz);
        gen.writeEndObject();
    }
    
    public static WSUser fromBson(JsonParser parser) throws IOException {
        String userID = null;
        String firstName = null;
        String lastName = null;
        String email = null;
        String tz = null;
        String newPass = null;
        WSPolicy wsp = null;
        List<WSRole> userRoles = null;

        // Skip over START_OBJECT token.
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = parser.getCurrentName();
            parser.nextToken();
            switch (fieldname) {
                case "userid":
                    userID = parser.getText();
                    break;
                case "firstname":
                    firstName = parser.getText();
                    break;
                case "lastname":
                    lastName = parser.getText();
                    break;
                case "email":
                    email = parser.getText();
                    break;
                case "policy":
                    wsp = WSPolicy.fromBson(parser);
                    break;
                case "newpass":
                    newPass = parser.getText();
                    break;
                case "roles":
                    userRoles = new LinkedList<>();
                    // Pointing to start_array. Advance to START_OBJECT then
                    // hand off to WSRole.fromBson. After each such call we are
                    // pointing at an END_OBJECT token.
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        WSRole r = WSRole.fromBson(parser);
                        userRoles.add(r);
                    }
                    break;
                case "tz":
                    tz = parser.getText();
                    break;
            }
        }
        WSUser ret = new WSUser(userID, firstName, lastName, email, tz, wsp, userRoles);
        ret.setNewUserPassword(newPass);
        return ret;
    }
    
    public void print() {
        MessageFormat mf = new MessageFormat("USERID=''{1}'',FIRSTNAME=''{2}'',LASTNAME=''{3}'',ROLES=''{4}''");
        String roleList = null;
        for (WSRole r : this.getUserRoles()) {
            if (roleList == null) {
                roleList = r.getRoleName();
            } else {
                roleList = roleList + "," + r.getRoleName();
            }
        }
        
        System.out.println(mf.format(new Object[]{ this.userID, this.firstName, this.lastName, roleList }));
    }
}
