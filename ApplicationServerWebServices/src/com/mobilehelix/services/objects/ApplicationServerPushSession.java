/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.mobilehelix.services.interfaces.WSRequest;
import com.mobilehelix.services.interfaces.WSResponse;
import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author shallem
 */
public class ApplicationServerPushSession extends WSRequest {
    private String uniqueID;
    private String clientid;
    private String userid;
    private String password;
    private String combinedUser;
    private String userEmail;
    private String settings;
    private List<Long> profileIDs;
    private Long appID;
    
    public ApplicationServerPushSession() {
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getClientid() {
        return clientid;
    }

    public void setClientid(String clientid) {
        this.clientid = clientid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getAppID() {
        return appID;
    }

    public void setAppID(Long appID) {
        this.appID = appID;
    }

    public String getCombinedUser() {
        return combinedUser;
    }

    public void setCombinedUser(String combinedUser) {
        this.combinedUser = combinedUser;
    }

    public String getUserEmail() {
        if (this.userEmail == null || this.userEmail.isEmpty()) {
            return this.userid;
        }
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public List<Long> getProfileIDs() {
        return profileIDs;
    }

    public void setProfileIDs(List<Long> profileIDs) {
        this.profileIDs = profileIDs;
    }
    
    @Override
    public byte[] toBson() throws IOException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
       
        JsonGenerator gen = factory.createJsonGenerator(baos);
        this.serializeObject(gen);
        gen.close();
        return baos.toByteArray();        
    }
    
    public void serializeObject(JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        // write out fields.
        gen.writeStringField("id", this.uniqueID);
        gen.writeStringField("client", this.clientid);
        if (this.userid != null) {
            gen.writeStringField("user", this.userid);
        }
        if (this.password != null) {
            gen.writeStringField("password", this.password);
        }
        if (this.userEmail != null) {
            gen.writeStringField("email", this.userEmail);
        }
        if (this.settings != null) {
            gen.writeStringField("settings", this.settings);
        }
        if (this.profileIDs != null) {
            gen.writeArrayFieldStart("profileids");
            for (Long l : this.profileIDs) {
                gen.writeNumber(l);
            }
            gen.writeEndArray();
        }
        gen.writeNumberField("appid", appID);
        gen.writeStringField("combined", this.combinedUser);
        gen.writeEndObject();
    }
    
    public static ApplicationServerPushSession fromBson(byte[] data) throws IOException {
        JsonParser parser = WSResponse.InitFromBSON(data);
        return ApplicationServerPushSession.fromBson(parser);
    }

    public static ApplicationServerPushSession fromBson(JsonParser parser) throws IOException {
        ApplicationServerPushSession asps = new ApplicationServerPushSession();
        // When we start here, the parser should be on a START_OBJECT token. Move forward to END_OBJECT or
        // KEY_NAME
        JsonToken nxtTok = parser.nextToken();
        while (nxtTok != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            // Move past field name token.
            nxtTok = parser.nextToken();
            switch (fieldName) {
                case "id":
                    asps.setUniqueID(parser.getText());
                    break;
                case "client":
                    asps.setClientid(parser.getText());
                    break;
                case "user":
                    asps.setUserid(parser.getText());
                    break;
                case "password":
                    asps.setPassword(parser.getText());
                    break;
                case "appid":
                    asps.setAppID(parser.getLongValue());
                    break;
                case "combined":
                    asps.setCombinedUser(parser.getText());
                    break;
                case "email":
                    asps.setUserEmail(parser.getText());
                    break;
                case "settings":
                    asps.setSettings(parser.getText());
                    break;
                case "profileids":
                    List<Long> pids = new LinkedList<>();
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        pids.add(parser.getLongValue());
                    }
                    asps.setProfileIDs(pids);
                    break;
            }
            // Move past the field value to the next field name or END_OBJECT
            nxtTok = parser.nextToken();
        }

        return asps;
    }
}
