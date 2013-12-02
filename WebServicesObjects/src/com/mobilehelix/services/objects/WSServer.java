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

/**
 *
 * @author shallem
 */
public class WSServer implements Comparable {

    private Long serverID;
    private String serverName;
    private byte[] sessionID;
    private Integer serverType;
    private String publicIPAddress;
    private String privateIPAddress;
    private Integer pubPort;
    private Integer privPort;
    private Integer status;
    private String region;
    private Date lastContact;
    private Date lastSucessfulContact;
    private Integer[] appTypes;
    private String additionalClients;
    
    public WSServer() {
    }
    
    /**
     * Used when we are creating a server record during the install process for a gateway,
     * mongo server, etc. and prior to registering that server.
     * @param serverName
     * @param serverType
     * @param region 
     */
    public WSServer(String serverName,
            Integer serverType,
            String region) {
        this.serverName = serverName;
        this.serverType = serverType;
        this.region = region;
    }
    
    public WSServer(String serverName,
            Integer serverType,
            String publicIP,
            Integer pubPort,
            String privateIP,
            Integer privPort,
            String region,
            Integer[] appTypes) {
        this.serverName = serverName;
        this.serverType = serverType;
        this.publicIPAddress = publicIP;
        this.privateIPAddress = privateIP;
        this.pubPort = pubPort;
        this.privPort = privPort;
        this.region = region;
        this.status = null;
        this.appTypes = appTypes;
        this.lastContact = null;
        this.lastSucessfulContact = null;
    }

    public WSServer(String serverName,
            Integer serverType,
            String publicIP,
            String privateIP,
            Integer pubport,
            Integer privport,
            Integer status,
            String region,
            Date lastContact,
            Date lastSuccessfulContact,
            Integer[] appTypes,
            String additionalClients) {
        this.serverName = serverName;
        this.serverType = serverType;
        this.publicIPAddress = publicIP;
        this.privateIPAddress = privateIP;
        this.pubPort = pubport;
        this.privPort = privport;
        this.status = status;
        this.region = region;
        this.lastContact = lastContact;
        this.lastSucessfulContact = lastSuccessfulContact;
        this.appTypes = appTypes;
        this.additionalClients = additionalClients;
    }
    
    /**
     * Partial constructor used to send a forwarding destination for a message. Primarily
     * this is used to indicate that a message sent to a Gateway should be forwarded
     * along to another server. That forwardee's information is encapsulated in this
     * object.
     * 
     * @param publicIP
     * @param publicPort
     * @param sessionID
     * @param appTypes 
     */
    public WSServer(String serverName,
            Long serverID,
            Integer status,
            String pubIP,
            Integer pubPort,
            String privIP,
            Integer privPort,
            String region,
            byte[] sessionID,
            int serverType,
            Date lastContact,
            Integer[] appTypes,
            String additionalClients) {
        this.serverName = serverName;
        this.serverID = serverID;
        this.status = status;
        this.publicIPAddress = pubIP;
        this.pubPort = pubPort;
        this.privateIPAddress = privIP;
        this.privPort = privPort;
        this.region = region;
        this.sessionID = sessionID;
        this.serverType = serverType;
        this.appTypes = appTypes;
        this.lastContact = lastContact;
        this.additionalClients = additionalClients;
    }
    
    
    public String getServerName() {
        return this.serverName;
    }

    public Date getLastContact() {
        return lastContact;
    }

    public Date getLastSucessfulContact() {
        return lastSucessfulContact;
    }

    public Integer getPrivPort() {
        return privPort;
    }

    public String getPrivateIPAddress() {
        return privateIPAddress;
    }

    public Integer getPubPort() {
        return pubPort;
    }

    public String getPublicIPAddress() {
        return publicIPAddress;
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

    public Long getServerID() {
        return serverID;
    }

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

    public void setPublicIPAddress(String publicIPAddress) {
        this.publicIPAddress = publicIPAddress;
    }

    public void setPrivateIPAddress(String privateIPAddress) {
        this.privateIPAddress = privateIPAddress;
    }

    public void setPubPort(Integer pubPort) {
        this.pubPort = pubPort;
    }

    public void setPrivPort(Integer privPort) {
        this.privPort = privPort;
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

    public void setLastSucessfulContact(Date lastSucessfulContact) {
        this.lastSucessfulContact = lastSucessfulContact;
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
        if (this.publicIPAddress != null) {
            gen.writeFieldName("pubip");
            gen.writeString(this.publicIPAddress);
        }
        if (this.pubPort != null) {
            gen.writeFieldName("pubport");
            gen.writeNumber(this.pubPort);
        }
        if (this.privateIPAddress != null) {
            gen.writeFieldName("privip");
            gen.writeString(this.publicIPAddress);
        }
        if (this.privPort != null) {
            gen.writeFieldName("privport");
            gen.writeNumber(this.pubPort);
        }
        if (this.status != null) {
            gen.writeFieldName("status");
            gen.writeNumber(this.status);
        }
        if (this.lastContact != null) {
            gen.writeFieldName("lastcontact");
            gen.writeString(this.lastContact.toString());
        }
        if (this.lastSucessfulContact != null) {
            gen.writeFieldName("lastsuccess");
            gen.writeString(this.lastSucessfulContact.toString());
        }
        if (this.sessionID != null) {
            gen.writeFieldName("sessid");
            gen.writeBinary(this.sessionID);
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
                newServer.publicIPAddress = parser.getText();
                break;
            case "privip":
                newServer.privateIPAddress = parser.getText();
                break;
            case "pubport":
                newServer.pubPort = parser.getIntValue();
                break;
            case "privport":
                newServer.privPort = parser.getIntValue();
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
                    newServer.lastSucessfulContact = formatter.parse(lastSuccessfulContactStr);
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
            this.publicIPAddress, this.privateIPAddress, this.pubPort, 
            this.privPort, this.status, this.region, 
            this.lastContact != null ? this.lastContact.toString() : "null", 
            this.lastSucessfulContact != null ? this.lastSucessfulContact.toString() : "null",
            this.appTypes != null ? Arrays.toString(this.appTypes) : "null" }));
    }

    @Override
    public int compareTo(Object o) {
        WSServer oServer = (WSServer) o;
        return this.serverID.compareTo(oServer.serverID);
    }
}
