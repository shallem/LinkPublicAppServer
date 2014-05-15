/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author shallem
 */
public class WSServer implements Comparable {

    private Long serverID;
    private String serverName;
    private byte[] sessionID;
    private Integer serverType;
    private String publicIP;
    private String privateIP;
    private Integer publicPort;
    private Integer privatePort;
    private Integer status;
    private String region;
    private Date lastContact;
    private Date lastSuccessfulContact;
    private Integer[] appTypes;
    private String additionalClients;
    private Boolean pingServer;
    private String version;
    
    public WSServer() {
    }    
    
    public String getServerName() {
        return this.serverName;
    }

    @JsonIgnore
    public Date getLastContact() {
        return lastContact;
    }

    @JsonIgnore
    public Date getLastSuccessfulContact() {
        return lastSuccessfulContact;
    }

    public String getRegion() {
        return region;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer[] getAppTypes() {
        return appTypes;
    }

    public Integer getServerType() {
        return serverType;
    }
    
    public byte[] getSessionID() {
        return sessionID;
    }

    @JsonIgnore
    public Long getServerID() {
        return serverID;
    }

    @JsonProperty
    public void setServerID(Long serverID) {
        this.serverID = serverID;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setSessionID(byte[] sessionID) {
        this.sessionID = sessionID;
    }
    
    public void setSessionIDString(String s) {
        this.sessionID = s.getBytes();
    }

    public void setServerType(Integer serverType) {
        this.serverType = serverType;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setLastContact(Date lastContact) {
        this.lastContact = lastContact;
    }

    public void setLastSuccessfulContact(Date lastSucessfulContact) {
        this.lastSuccessfulContact = lastSucessfulContact;
    }

    public void setAppTypes(Integer[] appTypes) {
        this.appTypes = appTypes;
    }

    public String getAdditionalClients() {
        return additionalClients;
    }

    public void setAdditionalClients(String additionalClients) {
        this.additionalClients = additionalClients;
    }

    public String getPublicIP() {
        return publicIP;
    }

    public void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }

    public String getPrivateIP() {
        return privateIP;
    }

    public void setPrivateIP(String privateIP) {
        this.privateIP = privateIP;
    }

    public Integer getPublicPort() {
        return publicPort;
    }

    public void setPublicPort(Integer publicPort) {
        this.publicPort = publicPort;
    }

    public Integer getPrivatePort() {
        return privatePort;
    }

    public void setPrivatePort(Integer privatePort) {
        this.privatePort = privatePort;
    }

    public Boolean getPingServer() {
        return pingServer;
    }

    public void setPingServer(Boolean pingServer) {
        this.pingServer = pingServer;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
        
    public void fieldsToBSON(JsonGenerator gen) throws IOException {
        if (this.serverID != null) {
            gen.writeFieldName("id");
            gen.writeNumber(this.serverID);
        }
        if (this.serverName != null) {
            gen.writeFieldName("name");
            gen.writeString(this.serverName);
        }
        if (this.serverType != null) {
            gen.writeFieldName("type");
            gen.writeNumber(this.serverType);
        }
        if (this.region != null) {
            gen.writeFieldName("region");
            gen.writeString(this.region);
        }
        if (this.publicIP != null) {
            gen.writeFieldName("pubip");
            gen.writeString(this.publicIP);
        }
        if (this.publicPort != null) {
            gen.writeFieldName("pubport");
            gen.writeNumber(this.publicPort);
        }
        if (this.privateIP != null) {
            gen.writeFieldName("privip");
            gen.writeString(this.privateIP);
        }
        if (this.privatePort != null) {
            gen.writeFieldName("privport");
            gen.writeNumber(this.privatePort);
        }
        if (this.status != null) {
            gen.writeFieldName("status");
            gen.writeNumber(this.status);
        }
        if (this.lastContact != null) {
            gen.writeFieldName("lastcontact");
            gen.writeString(this.lastContact.toString());
        }
        if (this.lastSuccessfulContact != null) {
            gen.writeFieldName("lastsuccess");
            gen.writeString(this.lastSuccessfulContact.toString());
        }
        if (this.sessionID != null) {
            gen.writeFieldName("sessid");
            gen.writeBinary(this.sessionID);
        }
        if (this.pingServer != null) {
            gen.writeBooleanField("ping", pingServer);
        }
        if (this.appTypes != null) {
            gen.writeArrayFieldStart("apptypes");
            for (Integer i : this.appTypes) {
                gen.writeNumber(i);
            }
            gen.writeEndArray();
        }
    }

    public static boolean parseBsonField(JsonParser parser,
            String fieldName,
            WSServer newServer) throws IOException, ParseException {
        // Parse a single field from BSON. Return true if we parsed this field,
        // false if not.
        boolean parsed = true;
        switch (fieldName) {
            case "id":
                newServer.serverID = parser.getLongValue();
                break;
            case "name":
                newServer.serverName = parser.getText();
                break;
            case "type":
                newServer.serverType = parser.getIntValue();
                break;
            case "pubip":
                newServer.publicIP = parser.getText();
                break;
            case "privip":
                newServer.privateIP = parser.getText();
                break;
            case "pubport":
                newServer.publicPort = parser.getIntValue();
                break;
            case "privport":
                newServer.privatePort = parser.getIntValue();
                break;
            case "status":
                newServer.status = parser.getIntValue();
                break;
            case "region":
                newServer.region = parser.getText();
                break;
            case "lastcontact":
                {
                    SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                    String lastContactStr = parser.getText();
                    newServer.lastContact = formatter.parse(lastContactStr);
                    break;
                }
            case "lastsuccess":
                {
                    SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                    String lastSuccessfulContactStr = parser.getText();
                    newServer.lastSuccessfulContact = formatter.parse(lastSuccessfulContactStr);
                    break;
                }
            case "apptypes":
                LinkedList<Integer> appTypes = new LinkedList<Integer>();
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    appTypes.add(parser.getIntValue());
                }
                newServer.appTypes = new Integer[appTypes.size()];
                newServer.appTypes = appTypes.toArray(newServer.appTypes);
                break;
            case "sessid":
                newServer.sessionID = (byte[])parser.getEmbeddedObject();
                break;
            case "ping":
                newServer.pingServer = parser.getBooleanValue();
                break;
            default:
                parsed = false;
                break;
        }
        return parsed;
    }

    public void toBson(JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        this.fieldsToBSON(gen);
        gen.writeEndObject();
    }
    
    public static WSServer fromBson(JsonParser parser) throws IOException, ParseException {
        WSServer newServer = new WSServer();

        // When we start, parser is pointing to START_OBJECT token.
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            // Advance to the field value
            parser.nextToken();
            
            WSServer.parseBsonField(parser, fieldName, newServer);
        }

        return newServer;
    }

    public void print() {
        String fmt = "NAME=''{0}'',TYPE=''{1}'',PUBLICIP=''{2}'',PRIVATEIP=''{3}'',PUBPORT=''{4,number,#}'',PRIVPORT=''{5,number,#}'',STATUS=''{6}'',REGION=''{7}'',LASTCONTACT=''{8}'',LASTSUCCESSFULCONTACT=''{9}'',APPTYPES=''{10}''";
        MessageFormat mf = new MessageFormat(fmt);
        System.out.println(mf.format(new Object[]{this.serverName, this.serverType, 
            this.publicIP, this.privateIP, this.publicPort, 
            this.privatePort, this.status, this.region, 
            this.lastContact != null ? this.lastContact.toString() : "null", 
            this.lastSuccessfulContact != null ? this.lastSuccessfulContact.toString() : "null",
            this.appTypes != null ? Arrays.toString(this.appTypes) : "null" }));
    }

    @Override
    public int compareTo(Object o) {
        WSServer oServer = (WSServer) o;
        return this.serverID.compareTo(oServer.serverID);
    }
}
