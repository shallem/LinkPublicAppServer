/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import com.mobilehelix.services.interfaces.WSRequest;
import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public class CreateSessionRequest {
    private String deviceRegion;
    private Double deviceLatitude;
    private Double deviceLongitude;
    private String client;
    private String userID;
    private String password;
    private int sessionExpirationType;
    private long sessionDuration;
    private byte[] sessionKey;
    private String deviceType;
    private Long[] appIDs;
    private Integer[] appGenIDs;
    private byte[] serverSessionID;
    
    public CreateSessionRequest() {
    }
    
    public int getSessionExpirationType() {
        return sessionExpirationType;
    }
    
    public long getSessionDuration() {
        return sessionDuration;
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }

    public String getUserID() {
        return userID;
    }

    public String getPassword() {
        return password;
    }

    public String getClient() {
        return client;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public Long[] getAppIDs() {
        return appIDs;
    }

    public Integer[] getAppGenIDs() {
        return appGenIDs;
    }
    
    public void setClient(String client) {
        this.client = client;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSessionExpirationType(int sessionExpirationType) {
        this.sessionExpirationType = sessionExpirationType;
    }

    public void setSessionDuration(long sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setAppIDs(Long[] appIDs) {
        this.appIDs = appIDs;
    }

    public void setAppGenIDs(Integer[] appGenIDs) {
        this.appGenIDs = appGenIDs;
    }

    public String getDeviceRegion() {
        return deviceRegion;
    }

    public void setDeviceRegion(String deviceRegion) {
        this.deviceRegion = deviceRegion;
    }

    public Double getDeviceLatitude() {
        return deviceLatitude;
    }

    public void setDeviceLatitude(Double deviceLatitude) {
        this.deviceLatitude = deviceLatitude;
    }

    public Double getDeviceLongitude() {
        return deviceLongitude;
    }

    public void setDeviceLongitude(Double deviceLongitude) {
        this.deviceLongitude = deviceLongitude;
    }

    public byte[] getServerSessionID() {
        return serverSessionID;
    }

    public void setServerSessionID(byte[] serverSessionID) {
        this.serverSessionID = serverSessionID;
    }
    
    public byte[] toBson() throws IOException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        JsonGenerator gen = factory.createJsonGenerator(baos);
        gen.writeStartObject();
	gen.writeBinaryField("serverkey", this.serverSessionID);
        gen.writeFieldName("userid");
        gen.writeString(this.userID);
        gen.writeFieldName("password");
        gen.writeString(this.password);
        gen.writeFieldName("expiretype");
        gen.writeNumber(sessionExpirationType);
        gen.writeFieldName("duration");
        gen.writeNumber(this.sessionDuration);
        gen.writeFieldName("sesskey");
        gen.writeBinary(this.sessionKey);
        gen.writeFieldName("devicetype");
        gen.writeString(this.deviceType);
        gen.writeFieldName("region");
        gen.writeString(this.deviceRegion);
        gen.writeFieldName("client");
        gen.writeString(this.client);
        gen.writeFieldName("lat");
        gen.writeNumber(this.deviceLatitude);
        gen.writeFieldName("long");
        gen.writeNumber(this.deviceLongitude);
        if (this.appIDs != null) {
            gen.writeArrayFieldStart("apps");
            for (Long appID : this.appIDs) {
                gen.writeNumber(appID);
            }
            gen.writeEndArray();
        }
        if (this.appGenIDs != null) {
            gen.writeArrayFieldStart("appgens");
            for (Integer appGenID : this.appGenIDs) {
                gen.writeNumber(appGenID);
            }
            gen.writeEndArray();
        }
        gen.writeEndObject();
        gen.close();
        return baos.toByteArray();
    }
    
    public static CreateSessionRequest fromBson(byte[] data) throws IOException,
            ParseException {
        CreateSessionRequest ret = new CreateSessionRequest();
        List<Long> appIDs = new LinkedList<>();
        List<Integer> appGenIDs = new LinkedList<>();
        
        // Input should be pointing to START_OBJECT token.
        JsonParser parser = WSRequest.InitFromBSON(data);
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = parser.getCurrentName();
            parser.nextToken();
            switch (fieldname) {
                case "client":
                    ret.client = parser.getText();
                    break;
                case "userid":
                    ret.userID = parser.getText();
                    break;
                case "password":
                    ret.password = parser.getText();
                    break;
                case "devicetype":
                    ret.deviceType = parser.getText();
                    break;
                case "region":
                    ret.deviceRegion = parser.getText();
                    break;
                case "duration":
                    ret.sessionDuration = parser.getLongValue();
                    break;
                case "sesskey":
                    ret.sessionKey = (byte[])parser.getEmbeddedObject();
                    break;
                case "expiretype":
                    ret.sessionExpirationType = parser.getIntValue();
                    break;
                case "apps":
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        Long i = parser.getLongValue();
                        appIDs.add(i);
                    }
                    ret.appIDs = new Long[appIDs.size()];
                    ret.appIDs = appIDs.toArray(ret.appIDs);
                    break;
                case "appgens":
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        Integer i = parser.getIntValue();
                        appGenIDs.add(i);
                    }
                    ret.appGenIDs = new Integer[appGenIDs.size()];
                    ret.appGenIDs = appGenIDs.toArray(ret.appGenIDs);
                    break;
                case "lat":
                    ret.setDeviceLatitude(parser.getDoubleValue());
                    break;
                case "long":
                    ret.setDeviceLongitude(parser.getDoubleValue());
                    break;
                case "serverkey":
                    ret.setServerSessionID((byte[])parser.getEmbeddedObject());
                    break;
            }
        }
        
        return ret;
    }
}
