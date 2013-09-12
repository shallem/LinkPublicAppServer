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
public class WSClient {
    private String name;
    private String clientID;
    private String jksPassword;
    private String dbPassword;
    private byte[] jksBytes;
    
    private WSClient() {
        
    }
    
    /**
     * Constructor primarily used to create new clients.
     * @param name Client name.
     * @param id Unique client UDID.
     * @param dbPassword Database password.
     * @param jksPwd Keystore password.
     */
    public WSClient(String name,
            String id,
            String dbPassword,
            String jksPwd) {
        this.name = name;
        this.clientID = id;
        this.jksPassword = jksPwd;
        this.dbPassword = dbPassword;
        this.jksBytes = null;
    }
    
    public WSClient(String name,
            String id,
            byte[] jksBytes) {
        this.name = name;
        this.clientID = id;
        this.jksPassword = null;
        this.jksBytes = jksBytes;
    }

    public String getName() {
        return this.name;
    }
    
    public String getClientID() {
        return this.clientID;
    }
    
    public String getDBPassword() {
        return this.dbPassword;
    }
    
    public String getJKSPassword() {
        return this.jksPassword;
    }
    
    public byte[] getJKSBytes() {
        return this.jksBytes;
    }
    
    public void toBson(JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("client");
        gen.writeString(this.getName());
        if (this.dbPassword != null) {
            gen.writeFieldName("dbpwd");
            gen.writeString(this.getDBPassword());
        }
        gen.writeFieldName("id");
        gen.writeString(this.getClientID());
        if (this.getJKSPassword() != null) {
            gen.writeFieldName("jkspass");
            gen.writeString(this.getJKSPassword());
        }
        if (this.getJKSBytes() != null) {
            gen.writeFieldName("jksb");
            gen.writeBinary(this.getJKSBytes());
        }
        gen.writeEndObject();
    }
    
    public static WSClient fromBson(JsonParser parser) throws IOException {
        WSClient c= new WSClient();
        
        // Skip over START_OBJECT token.
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = parser.getCurrentName();
            parser.nextToken();
            if ("client".equals(fieldname)) {
                c.name = parser.getText();
            } else if ("id".equals(fieldname)) {
                c.clientID = parser.getText();
            } else if ("dbpwd".equals(fieldname)) {
                c.dbPassword = parser.getText();
            } else if ("jkspass".equals(fieldname)) {
                c.jksPassword = parser.getText();
            } else if ("jksb".equals(fieldname)) {
                c.jksBytes = (byte[])parser.getEmbeddedObject();
            }
        }
        return c;
    }
    
    public void print() {
        MessageFormat mf = new MessageFormat("CLIENT=''{0}'',ID=''{1}''");        
        System.out.println(mf.format(new Object[]{ this.getName(), this.getClientID() }));
    }
}
