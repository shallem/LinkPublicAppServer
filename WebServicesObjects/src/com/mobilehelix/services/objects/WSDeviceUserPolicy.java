package com.mobilehelix.services.objects;

import com.mobilehelix.services.objects.WSExtra;
import com.mobilehelix.services.objects.WSPolicy;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 * Restricted set of policies that are relevant or enforced on the device. Sent
 * back to the device in the authentication procedure.
 * 
 * @author shallem
 */
public class WSDeviceUserPolicy {
    
    // Is authentication delegated?
    private Boolean authIsDelegated;
    
    // Offline auth policy.
    private Integer offlineAuthMethod;
    private Integer offlineSessionTimeoutMethod;
    private Integer offlineSessionTimeoutDuration;
    
    // Pincode policy
    private Integer pincodeComplexity;
    private Integer pincodeLength;
    
    // Extras
    private List<WSExtra> policyExtras;
    
    public WSDeviceUserPolicy() {
        
    }
    
    public WSDeviceUserPolicy(WSPolicy p) {
        this.authIsDelegated = p.isAuthIsDelegated();
        this.offlineSessionTimeoutMethod = p.getOfflineSessionTimeoutMethod();
        this.offlineSessionTimeoutDuration = p.getOfflineSessionTimeoutDuration();
        this.offlineAuthMethod = p.getOfflineAuthMethod();
        this.pincodeComplexity = p.getPincodeComplexity();
        this.pincodeLength = p.getPincodeLength();
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

    public Boolean getAuthIsDelegated() {
        return authIsDelegated;
    }

    public void setAuthIsDelegated(Boolean authIsDelegated) {
        this.authIsDelegated = authIsDelegated;
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
    
    public List<WSExtra> getPolicyExtras() {
        return policyExtras;
    }

    public void setPolicyExtras(List<WSExtra> policyExtras) {
        this.policyExtras = policyExtras;
    }
    
    public void toBson(JsonGenerator gen) throws IOException {
        gen.writeStartObject();
     
        if (this.authIsDelegated != null) {
            gen.writeFieldName("auth");
            gen.writeBoolean(authIsDelegated);
        }
        
        if (this.offlineAuthMethod >= 0) {
            gen.writeFieldName("offlineauth");
            gen.writeNumber(this.offlineAuthMethod);
            if (this.offlineAuthMethod > 0 && this.offlineSessionTimeoutMethod != null) {
                // Offline authentication is allowed.
                gen.writeFieldName("offlinetimeout");
                gen.writeNumber(this.offlineSessionTimeoutMethod);
                if (this.offlineSessionTimeoutDuration != null) {
                    gen.writeFieldName("offlineduration");
                    gen.writeNumber(this.offlineSessionTimeoutDuration);
                }
                
                // Send the pincode policies.
                gen.writeFieldName("pincomplexity");
                gen.writeNumber(this.pincodeComplexity);
                if (this.pincodeLength != null) {
                    gen.writeFieldName("pinlength");
                    gen.writeNumber(this.pincodeLength);
                }
            }
        }
            
        if (this.policyExtras != null &&
                !this.policyExtras.isEmpty()) {
            gen.writeArrayFieldStart("extras");
            for (WSExtra e : this.policyExtras) {
                e.toBson(gen, WSExtra.SerializeOptions.INCLUDE_ALL);
            }
            gen.writeEndArray();
        }
        
        gen.writeEndObject();
    }
    
    public static WSDeviceUserPolicy fromBson(JsonParser parser) throws IOException {
        WSDeviceUserPolicy wsp = new WSDeviceUserPolicy();
        
        // When we start, parser is pointing to START_OBJECT token.
       while (parser.nextToken() != JsonToken.END_OBJECT) {
           String fieldName = parser.getCurrentName();
           // Advance to the field value
           parser.nextToken();
            switch (fieldName) {
                case "offlineauth":
                    wsp.setOfflineAuthMethod(parser.getIntValue());
                    break;
                case "offlinetimeout":
                    wsp.setOfflineSessionTimeoutMethod(parser.getIntValue());
                    break;
                case "offlineduration":
                    wsp.setOfflineSessionTimeoutDuration(parser.getIntValue());
                    break;
                case "auth":
                    wsp.setAuthIsDelegated(parser.getBooleanValue());
                    break;
                case "extras":
                    LinkedList<WSExtra> attachedExtras = new LinkedList<>();
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        WSExtra e = WSExtra.fromBson(parser);
                        attachedExtras.add(e);
                    }
                    wsp.setPolicyExtras(attachedExtras);
                    break;
            }
       }
       
       return wsp;
    }

    @Override
    public String toString() {
        String policyFmt = "OFFLINEAUTH:''{0}'',OFFLINEMETHOD:''{1}'',OFFLINETIMEOUT:''{2}''";
        String msg = MessageFormat.format(policyFmt, new Object[]{
            this.offlineAuthMethod,
            this.offlineAuthMethod > 0 ? this.offlineSessionTimeoutMethod : "None",
            this.offlineSessionTimeoutDuration != null ? this.offlineSessionTimeoutDuration : "None"
        });
        return msg;
    }
    
    public void print() {
        System.out.println(MessageFormat.format("'{' {0} '}'", new Object[]{ this.toString() }));
    }
}
