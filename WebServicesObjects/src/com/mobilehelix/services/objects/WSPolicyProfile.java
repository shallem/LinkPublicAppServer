/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 * Encapsulates the policy for a single app.
 * 
 * @author shallem
 */
public class WSPolicyProfile {
    private Long appID;
    private String profileName;
    private String profileType;
    private List<WSExtraGroup> extras;
    private List<String> profileApplications;
    
    public WSPolicyProfile() {
        
    }

    public long getAppID() {
        return appID;
    }

    public void setAppID(long appID) {
        this.appID = appID;
    }
    
    public List<WSExtraGroup> getExtras() {
        return extras;
    }

    public void setExtras(List<WSExtraGroup> extras) {
        this.extras = extras;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileType() {
        return profileType;
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;
    }

    public List<String> getProfileApplications() {
        return profileApplications;
    }

    public void setProfileApplications(List<String> profileApplications) {
        this.profileApplications = profileApplications;
    }
    
    public void toBson(JsonGenerator gen, WSExtra.SerializeOptions serializeOptions) throws IOException {
        gen.writeStartObject();
        if (this.appID != null) {
            gen.writeNumberField("appid", appID);
        }
        if (this.profileName != null) {
            gen.writeStringField("name", this.profileName);
        }
        if (this.profileType != null) {
            gen.writeStringField("type", this.profileType);
        }
        if (this.profileApplications != null) {
            gen.writeArrayFieldStart("apps");
            for (String appName : this.profileApplications) {
                gen.writeString(appName);
            }
            gen.writeEndArray();
        }
        gen.writeArrayFieldStart("extras");
        for (WSExtraGroup eg : extras) {
            eg.toBson(gen, serializeOptions);
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }
    
    public static WSPolicyProfile fromBson(JsonParser parser) throws IOException {
        WSPolicyProfile profile = new WSPolicyProfile();
        
        // When we start, parser is pointing to START_OBJECT token.
        while (parser.nextToken() != JsonToken.END_OBJECT) {
           String fieldName = parser.getCurrentName();
           // Advance to the field value
           parser.nextToken();
            switch (fieldName) {
                case "appid":
                    profile.setAppID(parser.getLongValue());
                    break;
                case "name":
                    profile.setProfileName(parser.getText());
                    break;
                case "type":
                    profile.setProfileType(parser.getText());
                    break;
                case "apps":
                    LinkedList<String> appNames = new LinkedList<>();
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        String s = parser.getText();
                        appNames.add(s);
                    }
                    profile.setProfileApplications(appNames);
                    break;
                case "extras":
                    LinkedList<WSExtraGroup> attachedExtrasGroups = new LinkedList<>();
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        WSExtraGroup g = WSExtraGroup.fromBson(parser);
                        attachedExtrasGroups.add(g);
                    }
                    profile.setExtras(attachedExtrasGroups);
                    break;
            }
        }
        
        return profile;
    }
}
