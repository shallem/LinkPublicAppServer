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
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * @author shallem
 */
public class ApplicationServerSession extends WSRequest {
    private byte[] sessID;
    private String clientid;
    private String userid;
    private String password;
    private String deviceType;
    private String userEmail;
    private String legacyUserID;
    private Long deviceID;
    private Map<String, String> attributeMap;
    
    public ApplicationServerSession() {
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

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public byte[] getSessID() {
        return sessID;
    }

    public void setSessID(byte[] sessID) {
        this.sessID = sessID;
    }

    public String getLegacyUserID() {
        return legacyUserID;
    }

    public void setLegacyUserID(String legacyUserID) {
        this.legacyUserID = legacyUserID;
    }

    public Long getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(Long deviceID) {
        this.deviceID = deviceID;
    }

    public Map<String, String> getAttributeMap() {
        return attributeMap;
    }

    public void setAttributeMap(Map<String, String> attributeMap) {
        this.attributeMap = attributeMap;
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
    
    @Override
    public byte[] toBson() throws IOException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
       
        try (JsonGenerator gen = factory.createJsonGenerator(baos)) {
            this.serializeObject(gen);
        }
        return baos.toByteArray();        
    }
    
    public void serializeObject(JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        // write out fields.
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
        if (this.deviceType != null) {
            gen.writeStringField("device", this.deviceType);
        }
        gen.writeNumberField("deviceid", this.deviceID);
        if (this.legacyUserID != null) {
            gen.writeStringField("legacyuserid", this.legacyUserID);
        }
        gen.writeBinaryField("sessid", sessID);
        if (this.attributeMap != null && !this.attributeMap.isEmpty()) {
            gen.writeFieldName("attributes");
            gen.writeStartObject();
            for (Entry<String, String> e : this.attributeMap.entrySet()) {
                gen.writeStringField(e.getKey(), e.getValue());
            }
            gen.writeEndObject();
        }
        gen.writeEndObject();
    }
    
    public static ApplicationServerSession fromBson(byte[] data) throws IOException {
        JsonParser parser = WSResponse.InitFromBSON(data);
        return ApplicationServerSession.fromBson(parser);
    }

    public static ApplicationServerSession fromBson(JsonParser parser) throws IOException {
        ApplicationServerSession asps = new ApplicationServerSession();
        // When we start here, the parser should be on a START_OBJECT token. Move forward to END_OBJECT or
        // KEY_NAME
        JsonToken nxtTok = parser.nextToken();
        while (nxtTok != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            // Move past field name token.
            nxtTok = parser.nextToken();
            switch (fieldName) {
                case "client":
                    asps.setClientid(parser.getText());
                    break;
                case "user":
                    asps.setUserid(parser.getText());
                    break;
                case "password":
                    asps.setPassword(parser.getText());
                    break;
                case "device":
                    asps.setDeviceType(parser.getText());
                    break;
                case "deviceid":
                    asps.setDeviceID(parser.getLongValue());
                    break;
                case "legacyuserid":
                    asps.setLegacyUserID(parser.getText());
                    break;
                case "email":
                    asps.setUserEmail(parser.getText());
                    break;
                case "sessid":
                    asps.setSessID((byte[])parser.getEmbeddedObject());
                    break;
                case "attributes":
                    Map<String, String> attrs = new TreeMap<>();
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        String k = parser.getCurrentName();
                        parser.nextToken();
                        String v = parser.getText();
                        attrs.put(k, v);
                    }
                    asps.setAttributeMap(attrs);
                    break;
            }
            // Move past the field value to the next field name or END_OBJECT
            nxtTok = parser.nextToken();
        }

        return asps;
    }
}
