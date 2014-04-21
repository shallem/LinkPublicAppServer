/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public class WSPolicy {
    
    // Policy name.
    private String policyName;
    
    // Authentication policy.
    private boolean authIsDelegated;
    private Long authDelegateID;
    private String authDelegatedName;
    private Integer passwordExpirationDays;
    
    // Session policy.
    private Integer sessionTimeoutMethod;
    private Integer sessionTimeoutDurationMethod;
    private Integer sessionTimeoutDuration;

    // Offline auth policy.
    private Integer offlineAuthMethod;
    private Integer offlineSessionTimeoutMethod;
    private Integer offlineSessionTimeoutDuration;
    
    // Pincode policy.
    private Integer pincodeComplexity;
    private Integer pincodeLength;
    private Integer pincodeExpirationDuration;
    
    // Extras
    private List<WSPolicyProfile> appPolicyProfiles;
    
    public WSPolicy() {
        this.policyName = "Computed";
        this.authIsDelegated = false;
    }
    
    public WSPolicy(String policyName) {
        this.policyName = policyName;
        this.authIsDelegated = false;
    }
    
    public String getPolicyName() {
        return this.policyName;
    }
    
    public void setPolicyName(String s) {
        this.policyName = s;
    }
    
    public boolean isAuthIsDelegated() {
        return authIsDelegated;
    }

    public void setAuthIsDelegated(boolean authIsDelegated) {
        this.authIsDelegated = authIsDelegated;
    }

    public Long getAuthDelegateID() {
        return authDelegateID;
    }

    public void setAuthDelegateID(Long authDelegateID) {
        this.authDelegateID = authDelegateID;
    }

    public Integer getSessionTimeoutMethod() {
        return sessionTimeoutMethod;
    }

    public void setSessionTimeoutMethod(Integer sessionTimeoutMethod) {
        this.sessionTimeoutMethod = sessionTimeoutMethod;
    }

    public Integer getSessionTimeoutDurationMethod() {
        return sessionTimeoutDurationMethod;
    }

    public void setSessionTimeoutDurationMethod(Integer sessionTimeoutDurationMethod) {
        this.sessionTimeoutDurationMethod = sessionTimeoutDurationMethod;
    }

    public Integer getSessionTimeoutDuration() {
        return sessionTimeoutDuration;
    }

    public void setSessionTimeoutDuration(Integer sessionTimeoutDuration) {
        this.sessionTimeoutDuration = sessionTimeoutDuration;
    }

    public Integer getOfflineSessionTimeoutMethod() {
        return offlineSessionTimeoutMethod;
    }

    public void setOfflineSessionTimeoutMethod(Integer offlineSessionTimeoutMethod) {
        this.offlineSessionTimeoutMethod = offlineSessionTimeoutMethod;
    }

    public Integer getOfflineSessionTimeoutDuration() {
        return offlineSessionTimeoutDuration;
    }

    public void setOfflineSessionTimeoutDuration(Integer offlineSessionTimeoutDuration) {
        this.offlineSessionTimeoutDuration = offlineSessionTimeoutDuration;
    }

    public Integer getOfflineAuthMethod() {
        return offlineAuthMethod;
    }

    public void setOfflineAuthMethod(Integer offlineAuthMethod) {
        this.offlineAuthMethod = offlineAuthMethod;
    }

    public Integer getPincodeExpirationDuration() {
        return pincodeExpirationDuration;
    }

    public void setPincodeExpirationDuration(Integer pincodeExpirationDuration) {
        this.pincodeExpirationDuration = pincodeExpirationDuration;
    }

    public List<WSPolicyProfile> getAppPolicyProfiles() {
        return appPolicyProfiles;
    }

    public void setAppPolicyProfiles(List<WSPolicyProfile> appPolicyProfiles) {
        this.appPolicyProfiles = appPolicyProfiles;
    }

    public Integer getPincodeComplexity() {
        return pincodeComplexity;
    }

    public void setPincodeComplexity(Integer pincodeComplexity) {
        this.pincodeComplexity = pincodeComplexity;
    }

    public Integer getPincodeLength() {
        return pincodeLength;
    }

    public void setPincodeLength(Integer pincodeLength) {
        this.pincodeLength = pincodeLength;
    }

    public Integer getPasswordExpirationDays() {
        return passwordExpirationDays;
    }

    public void setPasswordExpirationDays(Integer passwordExpirationDays) {
        this.passwordExpirationDays = passwordExpirationDays;
    }

    public String getAuthDelegatedName() {
        return authDelegatedName;
    }

    public void setAuthDelegatedName(String authDelegatedName) {
        this.authDelegatedName = authDelegatedName;
    }
    
    public void toBson(JsonGenerator gen, WSExtra.SerializeOptions options) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("name");
        gen.writeString(this.policyName);
        
        gen.writeFieldName("delauth");
        gen.writeBoolean(authIsDelegated);
        if (authIsDelegated) {
            if (this.authDelegateID != null) {
                gen.writeNumberField("delauthid", this.authDelegateID);
            } else if (this.authDelegatedName != null) {
                gen.writeStringField("delauthname", this.authDelegatedName);
            }
        }
        if (this.passwordExpirationDays != null) {
            gen.writeFieldName("passexpire");
            gen.writeNumber(this.passwordExpirationDays);
        }
        if (this.sessionTimeoutMethod != null) {
            gen.writeFieldName("sessto");
            gen.writeNumber(this.sessionTimeoutMethod);
        }
        if (this.sessionTimeoutDurationMethod != null) {
            gen.writeFieldName("sessdurmethod");
            gen.writeNumber(sessionTimeoutDurationMethod);
        }
        if (this.sessionTimeoutDuration != null) {
            gen.writeFieldName("sessdur");
            gen.writeNumber(sessionTimeoutDuration);
        }
        if (this.pincodeComplexity != null) {
            gen.writeFieldName("pincomplexity");
            gen.writeNumber(this.pincodeComplexity);
        }
        if (this.pincodeLength != null) {
            gen.writeFieldName("pinlength");
            gen.writeNumber(this.pincodeLength);
        }
        if (this.pincodeExpirationDuration != null) {
            gen.writeFieldName("pinexpire");
            gen.writeNumber(this.pincodeExpirationDuration);
        }
        
        if (this.offlineAuthMethod == null) {
            this.offlineAuthMethod = 0; // No offline authentication.
        }
        if (this.offlineAuthMethod >= 0) {
            gen.writeFieldName("offlineauth");
            gen.writeNumber(this.offlineAuthMethod);
            if (this.offlineAuthMethod > 0) {
                gen.writeFieldName("offlinemethod");
                gen.writeNumber(this.offlineAuthMethod);
                if (this.offlineSessionTimeoutMethod != null) {
                    gen.writeFieldName("offlinetimeout");
                    gen.writeNumber(this.offlineSessionTimeoutDuration);
                }
            }
        }
                    
        if (this.appPolicyProfiles != null &&
                !this.appPolicyProfiles.isEmpty()) {
            gen.writeArrayFieldStart("appprofiles");
            for (WSPolicyProfile entry : this.appPolicyProfiles) {
                entry.toBson(gen, options);
            }
            gen.writeEndArray();
        }
        
        gen.writeEndObject();
    }
    
    public static WSPolicy fromBson(JsonParser parser) throws IOException {
       WSPolicy wsp = new WSPolicy();
        
        // When we start, parser is pointing to START_OBJECT token.
       while (parser.nextToken() != JsonToken.END_OBJECT) {
           String fieldName = parser.getCurrentName();
           // Advance to the field value
           parser.nextToken();
            switch (fieldName) {
                case "name":
                    wsp.setPolicyName(parser.getText());
                    break;
                case "delauth":
                    wsp.setAuthIsDelegated(parser.getBooleanValue());
                    break;
                case "delauthid":
                    wsp.setAuthDelegateID(parser.getLongValue());
                    break;
                case "delauthname":
                    wsp.setAuthDelegatedName(parser.getText());
                    break;
                case "sessto":
                    wsp.setSessionTimeoutMethod(parser.getIntValue());
                    break;
                case "sessdurmethod":
                    wsp.setSessionTimeoutDurationMethod(parser.getIntValue());
                    break;
                case "sessdur":
                    wsp.setSessionTimeoutDuration(parser.getIntValue());
                    break;
                case "offlineauth":
                    wsp.setOfflineAuthMethod(parser.getIntValue());
                    break;
                case "offlinemethod":
                    wsp.setOfflineSessionTimeoutMethod(parser.getIntValue());
                    break;
                case "offlinetimeout":
                    wsp.setOfflineSessionTimeoutDuration(parser.getIntValue());
                    break;
                case "pincomplexity":
                    wsp.setPincodeComplexity(parser.getIntValue());
                    break;
                case "pinlength":
                    wsp.setPincodeLength(parser.getIntValue());
                    break;
                case "pinexpire":
                    wsp.setPincodeExpirationDuration(parser.getIntValue());
                    break;
                case "passexpire":
                    wsp.setPasswordExpirationDays(parser.getIntValue());
                    break;
                case "appprofiles":
                    LinkedList<WSPolicyProfile> appProfiles = new LinkedList<>();
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        WSPolicyProfile g = WSPolicyProfile.fromBson(parser);
                        appProfiles.add(g);
                    }
                    wsp.setAppPolicyProfiles(appProfiles);
                    break;
            }
       }
       
       return wsp;
    }
}
