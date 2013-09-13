/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import java.io.IOException;
import java.text.MessageFormat;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public class WSDevice {
    private String deviceNickName;
    private String deviceType;
    private String deviceOwnerID;
    private String deviceCarrierName;
    private String deviceServiceCountry;
    private Double deviceLatitude;
    private Double deviceLongitude;
    private Integer deviceState;
    private Boolean ownerIsRole;
    
    public WSDevice() {
    }
    
    public WSDevice(String deviceNickName,
            String deviceType,
            String deviceOwnerID,
            boolean ownerIsRole) {
        this.deviceNickName = deviceNickName;
        this.deviceType = deviceType;
        this.deviceOwnerID = deviceOwnerID;
        this.ownerIsRole = ownerIsRole;
    }
    
    public String getDeviceNickName() {
        return deviceNickName;
    }

    public String getDeviceOwnerID() {
        return deviceOwnerID;
    }

    public String getDeviceType() {
        return deviceType;
    }
    
    public boolean getOwnerIsRole() {
        return ownerIsRole;
    }

    public String getDeviceCarrierName() {
        return deviceCarrierName;
    }

    public void setDeviceCarrierName(String deviceCarrierName) {
        this.deviceCarrierName = deviceCarrierName;
    }

    public String getDeviceServiceCountry() {
        return deviceServiceCountry;
    }

    public void setDeviceServiceCountry(String deviceServiceCountry) {
        this.deviceServiceCountry = deviceServiceCountry;
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

    public Boolean isOwnerIsRole() {
        return ownerIsRole;
    }

    public void setOwnerIsRole(boolean ownerIsRole) {
        this.ownerIsRole = ownerIsRole;
    }

    public void setDeviceNickName(String deviceNickName) {
        this.deviceNickName = deviceNickName;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setDeviceOwnerID(String deviceOwnerID) {
        this.deviceOwnerID = deviceOwnerID;
    }

    public Integer getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(Integer deviceState) {
        this.deviceState = deviceState;
    }
    
    public void toBson(JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("name");
        gen.writeString(this.deviceNickName);
        gen.writeFieldName("type");
        gen.writeString(this.deviceType);
        if (this.deviceOwnerID != null) {
            gen.writeFieldName("owner");
            gen.writeString(this.deviceOwnerID);
        }
        if (this.deviceCarrierName != null) {
            gen.writeFieldName("carrier");
            gen.writeString(this.deviceCarrierName);
        }
        if (this.deviceServiceCountry != null) {
            gen.writeFieldName("country");
            gen.writeString(this.deviceServiceCountry);
        }
        if (this.deviceLatitude != null) {
            gen.writeFieldName("lat");
            gen.writeNumber(this.deviceLatitude);
        }
        if (this.deviceLongitude != null) {
            gen.writeFieldName("long");
            gen.writeNumber(this.deviceLongitude);
        }
        if (this.deviceState != null) {
            gen.writeFieldName("state");
            gen.writeNumber(this.deviceState);
        }
        if (this.ownerIsRole != null) {
            gen.writeFieldName("isrole");
            gen.writeBoolean(this.ownerIsRole);
        }
        gen.writeEndObject();
    }
    
    public static WSDevice fromBson(JsonParser parser) throws IOException {
        WSDevice wsd = new WSDevice();
        
        // When we start, parser is pointing to START_OBJECT token.
       while (parser.nextToken() != JsonToken.END_OBJECT) {
           String fieldName = parser.getCurrentName();
           // Advance to the field value
           parser.nextToken();
            switch (fieldName) {
                case "name":
                    wsd.setDeviceNickName(parser.getText());
                    break;
                case "type":
                    wsd.setDeviceType(parser.getText());
                    break;
                case "owner":
                    wsd.setDeviceOwnerID(parser.getText());
                    break;
                case "isrole":
                    wsd.setOwnerIsRole(parser.getBooleanValue());
                    break;
                case "carrier":
                    wsd.setDeviceCarrierName(parser.getText());
                    break;
                case "country":
                    wsd.setDeviceServiceCountry(parser.getText());
                    break;
                case "lat":
                    wsd.setDeviceLatitude(parser.getDoubleValue());
                    break;
                case "long":
                    wsd.setDeviceLongitude(parser.getDoubleValue());
                    break;
                case "state":
                    wsd.setDeviceState(parser.getIntValue());
                    break;
            }
       }
       
       return wsd;
    }

    public void print() {
        String fmt = "NICKNAME=''{0}'',TYPE=''{1}'',OWNERID=''{3}''";
        MessageFormat mf = new MessageFormat(fmt);
        System.out.println(mf.format(new Object[]{ this.deviceNickName, this.deviceType, this.deviceOwnerID }));
    }
}
