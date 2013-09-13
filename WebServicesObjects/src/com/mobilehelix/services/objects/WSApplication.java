package com.mobilehelix.services.objects;

import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

public class WSApplication {
    private String client;
    private String appName;
    private Long uniqueID;
    private String categoryname;
    private Integer appType;
    private Integer appGenId;
    private WSRole[] appRoles;
    private WSExtra[] appExtras;
    
    public WSApplication() {
        
    }
    
    public WSApplication(String client,
            String appName,
            Long uniqueID,
            String categoryname,
            Integer appType,
            int appGenId,
            List<WSRole> appRoles,
            List<WSExtra> extras) {
	this.init(client,
                appName,
                uniqueID,
                categoryname, 
                appType, 
                appGenId,
                appRoles,
                extras);
    }
    
    private void init(String client,
            String appName,
            Long uniqueID,
            String categoryname,
            Integer appType,
            int appGenId,
            List<WSRole> appRoles,
            List<WSExtra> appExtras) {
        this.client = client;
        this.appName = appName;
        this.uniqueID = uniqueID;
        this.categoryname = categoryname;
        this.appType = appType;
        this.appGenId = appGenId;
        
        if (appRoles != null && !appRoles.isEmpty()) {
            this.appRoles = new WSRole[appRoles.size()];
            this.appRoles = appRoles.toArray(this.appRoles);
        }
        
        if (appExtras != null && !appExtras.isEmpty()) {
            this.appExtras = new WSExtra[appExtras.size()];
            this.appExtras = appExtras.toArray(this.appExtras);
        }
    }
    
    public String getAppName() {
	return appName;
    }
    
    public String getCategoryname() {
        return categoryname;
    }
    
    public WSRole[] getAppRoles() {
        return appRoles;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setUniqueID(Long uniqueID) {
        this.uniqueID = uniqueID;
    }

    public void setCategoryname(String categoryname) {
        this.categoryname = categoryname;
    }

    public void setAppType(Integer appType) {
        this.appType = appType;
    }

    public void setAppGenId(int appGenId) {
        this.appGenId = appGenId;
    }

    public void setAppRoles(WSRole[] appRoles) {
        this.appRoles = appRoles;
    }

    public void setAppExtras(WSExtra[] appExtras) {
        this.appExtras = appExtras;
    }
    
    public WSExtra[] getAppExtras() {
        return appExtras;
    }

    public Integer getAppType() {
        return appType;
    }

    public int getAppGenID() {
        return this.appGenId;
    }
    
    public Long getUniqueID() {
        return uniqueID;
    }

    public int getAppGenId() {
        return appGenId;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }
    
    public byte[] toBson() throws IOException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        JsonGenerator gen = factory.createJsonGenerator(baos);
        this.toBson(gen);
        gen.close();
        return baos.toByteArray();
    }
    
    public void toBson(JsonGenerator gen) throws IOException {
	gen.writeStartObject();
        gen.writeStringField("client", client);
	gen.writeFieldName("name");
	gen.writeString(appName);
        if (this.appType != null) {
            gen.writeFieldName("type");
            gen.writeNumber(this.appType);
        }
        if (this.uniqueID != null) {
            gen.writeFieldName("id");
            gen.writeNumber(this.uniqueID);
        }
        if (categoryname != null) {
            gen.writeFieldName("category");
            gen.writeString(categoryname);
        }
        if (appRoles != null) {
            gen.writeArrayFieldStart("roles");
            for (WSRole r : appRoles) {
                r.toBson(gen);
            }
            gen.writeEndArray();
        }
        if (appExtras != null) {
            gen.writeArrayFieldStart("extras");
            for (WSExtra e : appExtras) {
                e.toBson(gen);
            }
            gen.writeEndArray();
        }
        if (this.appGenId != null) {
            gen.writeFieldName("genid");
            gen.writeNumber(this.appGenId);
        }
	gen.writeEndObject();
    }
    
    public void toJSONForDevice(JsonGenerator gen) throws IOException {
        gen.writeFieldName("name");
	gen.writeString(appName);
        if (this.uniqueID != null) {
            gen.writeFieldName("id");
            gen.writeNumber(this.uniqueID);
        }
        if (this.appGenId != null) {
            gen.writeFieldName("genid");
            gen.writeNumber(this.appGenId);
        }
        if (categoryname != null) {
            gen.writeFieldName("category");
            gen.writeString(categoryname);
        }
        if (appExtras != null) {
            gen.writeArrayFieldStart("extras");
            for (WSExtra e : appExtras) {
                e.toJSON(gen);
            }
            gen.writeEndArray();
        }
    }

    public static WSApplication fromBson(JsonParser parser) throws IOException {
	String appName = null;
        String client = null;
        Long uniqueID = null;        
        String category = null;
        Integer appType = null;
        List<WSRole> appRoles = null;
        List<WSExtra> appExtras = null;
        int genid = 0;
        
        // Input should be pointing to START_OBJECT token.
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = parser.getCurrentName();
            parser.nextToken();
            switch (fieldname) {
                case "client":
                    client = parser.getText();
                    break;
                case "name":
                    appName = parser.getText();
                    break;
                case "type":
                    appType = parser.getIntValue();
                    break;
                case "id":
                    uniqueID = new Long(parser.getIntValue());
                    break;
                case "category":
                    category = parser.getText();
                    break;
                case "roles":
                    appRoles = new LinkedList<>();
                    // Advance past start array.
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        // Should be pointing to START_OBJECT
                        WSRole r = WSRole.fromBson(parser);
                        appRoles.add(r);
                    }
                    break;
                case "extras":
                    appExtras = new LinkedList<>();
                    // Advance past start array.
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        // Should be pointing to START_OBJECT
                        WSExtra e = WSExtra.fromBson(parser);
                        appExtras.add(e);
                    }
                    break;
                case "genid":
                    genid = parser.getIntValue();
                    break;
            }
        }
        
	return new WSApplication(client,
                appName, 
                uniqueID, 
                category, 
                appType, 
                genid, 
                appRoles,
                appExtras);
    }
}
