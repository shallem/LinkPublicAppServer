/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import com.mobilehelix.services.interfaces.WSRequest;
import com.mobilehelix.services.interfaces.WSResponse;
import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public class ApplicationServerInitRequest extends WSRequest {
    // Parameters that we send to the app server service.
    private String controllerIP;
    private Integer controllerPort;
    private String asPubIP;
    private Integer asPubPort;
    private Integer asPrivPort;
    private Integer asHttpPort;
    private String clientName;
    private String serverName;
    private String pushServerName;
    private String storePass;
    private byte[] keyStore;
    private String regionName;

    public ApplicationServerInitRequest() {
        
    }
    
    public ApplicationServerInitRequest(String controllerIP,
            Integer controllerPort,
            String asPubIP,
            Integer asPubPort,
            Integer asPrivPort,
            Integer asHttpPort,
            String clientName,
            String serverName,
            String storePass,
            byte[] keyStore,
            String regionName) {
        this.controllerIP = controllerIP;
        this.controllerPort = controllerPort;
        this.asPubIP = asPubIP;
        this.asPubPort = asPubPort;
        this.asPrivPort = asPrivPort;
        this.asHttpPort = asHttpPort;
        this.clientName = clientName;
        this.serverName = serverName;
        this.pushServerName = this.serverName + "-PUSH";
        this.storePass = storePass;
        this.keyStore = keyStore;
        this.regionName = regionName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    
    public byte[] getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(byte[] keyStore) {
        this.keyStore = keyStore;
    }

    public String getControllerIP() {
        return controllerIP;
    }

    public void setControllerIP(String controllerIP) {
        this.controllerIP = controllerIP;
    }

    public Integer getControllerPort() {
        return controllerPort;
    }

    public void setControllerPort(Integer controllerPort) {
        this.controllerPort = controllerPort;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getPushServerName() {
        return pushServerName;
    }

    public void setPushServerName(String pushServerName) {
        this.pushServerName = pushServerName;
    }
    
    public String getStorePass() {
        return storePass;
    }

    public void setStorePass(String storePass) {
        this.storePass = storePass;
    }

    public String getAsPubIP() {
        return asPubIP;
    }

    public void setAsPubIP(String asPubIP) {
        this.asPubIP = asPubIP;
    }

    public Integer getAsPubPort() {
        return asPubPort;
    }

    public void setAsPubPort(Integer asPubPort) {
        this.asPubPort = asPubPort;
    }

    public Integer getAsPrivPort() {
        return asPrivPort;
    }

    public void setAsPrivPort(Integer asPrivPort) {
        this.asPrivPort = asPrivPort;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public Integer getAsHttpPort() {
        return asHttpPort;
    }

    public void setAsHttpPort(Integer asHttpPort) {
        this.asHttpPort = asHttpPort;
    }
    
    @Override
    public byte[] toBson() throws IOException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
       
        JsonGenerator gen = factory.createJsonGenerator(baos);
        gen.writeStartObject();
        // write out fields.
        gen.writeFieldName("cip");
        gen.writeString(this.controllerIP);
        gen.writeFieldName("cport");
        gen.writeNumber(this.controllerPort);
        gen.writeFieldName("pubip");
        gen.writeString(this.asPubIP);
        gen.writeFieldName("pubport");
        gen.writeNumber(this.asPubPort);
        gen.writeFieldName("privport");
        gen.writeNumber(this.asPrivPort);
        gen.writeNumberField("httpport", this.asHttpPort);
        gen.writeFieldName("client");
        gen.writeString(this.clientName);
        gen.writeFieldName("server");
        gen.writeString(this.serverName);
        gen.writeFieldName("push");
        gen.writeString(this.pushServerName);
        gen.writeFieldName("storepass");
        gen.writeString(this.storePass);
        gen.writeFieldName("keystore");
        gen.writeBinary(this.keyStore);        
        gen.writeFieldName("region");
        gen.writeString(this.regionName);

        gen.close();
        return baos.toByteArray();
    }
    
    public static ApplicationServerInitRequest fromBson(byte[] data) throws IOException {
        ApplicationServerInitRequest asir = new ApplicationServerInitRequest();
        JsonParser parser = WSResponse.InitFromBSON(data);
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            // Move past field name token.
            parser.nextToken();
            switch (fieldName) {
                case "cip":
                    asir.setControllerIP(parser.getText());
                    break;
                case "cport":
                    asir.setControllerPort(parser.getIntValue());
                    break;
                case "client":
                    asir.setClientName(parser.getText());
                    break;
                case "server":
                    asir.setServerName(parser.getText());
                    break;
                case "push":
                    asir.setPushServerName(parser.getText());
                    break;
                case "storepass":
                    asir.setStorePass(parser.getText());
                    break;
                case "keystore":
                    asir.setKeyStore((byte[])parser.getEmbeddedObject());
                    break;
                case "pubip":
                    asir.setAsPubIP(parser.getText());
                    break;
                case "region":
                    asir.setRegionName(parser.getText());
                    break;
                case "pubport":
                    asir.setAsPubPort(parser.getIntValue());
                    break;
                case "privport":
                    asir.setAsPrivPort(parser.getIntValue());
                    break;
                case "httpport":
                    asir.setAsHttpPort(parser.getIntValue());
                    break;
            }
        }
        return asir;
    }
}
