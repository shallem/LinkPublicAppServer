package com.mobilehelix.services.objects;

import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class WSApplication {
    private String client;
    private String appName;
    private Long uniqueID;
    private String categoryname;
    private Integer appType;
    private Integer appGenId;
    private WSRole[] appRoles;
    
    // Extras
    private Collection<WSExtraGroup> appExtraGroups;
    
    public WSApplication() {
        
    }
    
    public WSApplication(String client,
            String appName,
            Long uniqueID,
            String categoryname,
            Integer appType,
            int appGenId,
            List<WSRole> appRoles,
            Collection<WSExtraGroup> appExtrasGroups) {
	this.init(client,
                appName,
                uniqueID,
                categoryname, 
                appType, 
                appGenId,
                appRoles,
                appExtrasGroups);
    }
    
    private void init(String client,
            String appName,
            Long uniqueID,
            String categoryname,
            Integer appType,
            int appGenId,
            List<WSRole> appRoles,
            Collection<WSExtraGroup> appExtrasGroups) {
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
        
        if (appExtrasGroups != null && !appExtrasGroups.isEmpty()) {
            this.appExtraGroups = new ArrayList<>(appExtrasGroups.size());
            this.appExtraGroups.addAll(appExtrasGroups);
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

    @JsonProperty
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

    public Collection<WSExtraGroup> getAppExtraGroups() {
        return appExtraGroups;
    }

    public void setAppExtraGroups(Collection<WSExtraGroup> appExtraGroups) {
        this.appExtraGroups = appExtraGroups;
    }

    public Integer getAppType() {
        return appType;
    }

    public int getAppGenID() {
        return this.appGenId;
    }
    
    @JsonIgnore
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
    
    public byte[] toBson(WSExtra.SerializeOptions serializeOptions) throws IOException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        JsonGenerator gen = factory.createJsonGenerator(baos);
        this.toBson(gen, serializeOptions);
        gen.close();
        return baos.toByteArray();
    }
    
    public void toBson(JsonGenerator gen, WSExtra.SerializeOptions serializeOptions) throws IOException {
	gen.writeStartObject();
        if (client != null) {
            gen.writeStringField("client", client);
        }
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
        if (this.appExtraGroups != null &&
                !this.appExtraGroups.isEmpty()) {
            gen.writeArrayFieldStart("extraGroups");
            for (WSExtraGroup eg : this.appExtraGroups) {
                eg.toBson(gen, serializeOptions);
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
        if (appExtraGroups != null) {
            gen.writeArrayFieldStart("extras");
            for (WSExtraGroup eg : this.appExtraGroups) {
                for (WSExtra e : eg.getExtras()) {
                    e.toJSON(gen, WSExtra.SerializeOptions.DEVICE_ONLY);
                }
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
        int genid = 0;
        LinkedList<WSExtraGroup> attachedExtrasGroups = new LinkedList<>();
        
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
                case "extraGroups":
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        WSExtraGroup g = WSExtraGroup.fromBson(parser);
                        attachedExtrasGroups.add(g);
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
                attachedExtrasGroups);
    }
}
