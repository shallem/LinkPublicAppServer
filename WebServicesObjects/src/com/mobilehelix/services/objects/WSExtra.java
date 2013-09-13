/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import com.mobilehelix.services.constants.ExtraTypeConstants;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public class WSExtra {

    public String tag;
    public int dataType;
    public int schemaType;
    public String mergeFn;
    public Long groupID;
    public byte[] value;

    public WSExtra() {
        this.mergeFn = "default";
    }

    public WSExtra(String tag, String value) {
        this.tag = tag;
        this.dataType = ExtraTypeConstants.EXTRA_TYPE_STRING;
        this.schemaType = ExtraTypeConstants.EXTRA_SCHEMA_ATTRIBUTE;
        this.value = value.getBytes();
        this.groupID = null;
        this.mergeFn = "default";
    }

    public WSExtra(String tag, int dataType, int schemaType, String mergeFn, byte[] value) {
        this.tag = tag;
        this.dataType = dataType;
        this.schemaType = schemaType;
        this.value = value;
        this.groupID = null;
        this.mergeFn = mergeFn;
    }
    
    public WSExtra(String tag, int dataType, int schemaType, String mergeFn, Integer value) {
        this.tag = tag;
        this.dataType = dataType;
        this.schemaType = schemaType;
        try {
            this.value = value.toString().getBytes("US-ASCII");
        } catch(UnsupportedEncodingException ex) {
            // Ignore the exception b/c US-ASCII encoding is REQUIRED by all implementations
            // of Java.
        }
        this.groupID = null;
        this.mergeFn = mergeFn;
    }
    
    public WSExtra(String tag, int dataType, int schemaType, String mergeFn, Boolean value) {
        this.tag = tag;
        this.dataType = dataType;
        this.schemaType = schemaType;
        byte[] b = new byte[1];
        if (value != null && value) {
            b[0] = 1;
        } else {
            b[0] = 0;
        }
        this.value = b;
        this.groupID = null;
        this.mergeFn = mergeFn;
    }

    public WSExtra(String tag, int dataType, int schemaType, String mergeFn, byte[] value, Long gid) {
        this.tag = tag;
        this.dataType = dataType;
        this.schemaType = schemaType;
        this.value = value;
        this.groupID = gid;
        this.mergeFn = mergeFn;
    }

    public String getTag() {
        return tag;
    }

    public int getDataType() {
        return dataType;
    }

    public int getSchemaType() {
        return schemaType;
    }

    public String getValue() {
        if (this.value == null) {
            return "";
        }
        
        return new String(this.value);
    }
    
    public byte[] getValueBytes() {
        return this.value;
    }

    public Long getGroupID() {
        return groupID;
    }

    public void setGroupID(Long groupID) {
        this.groupID = groupID;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public void setSchemaType(int schemaType) {
        this.schemaType = schemaType;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
    
    public void setValueString(String s) {
        this.value = s.getBytes();
        this.dataType = ExtraTypeConstants.EXTRA_TYPE_STRING;
    }
    
    public void setValueStringList(String s) {
        this.value = s.getBytes();
        this.dataType = ExtraTypeConstants.EXTRA_TYPE_STRING_LIST;
    }
    
    public boolean getValueBoolean() {
        return (this.value[0] == 1);
    }
    
    public void setValueBoolean(String s) {
        byte[] b = new byte[1];
        if (s.equals("true") ||
                s.equals("True") ||
                s.equals("t")) {
            b[0] = 1;
        } else {
            b[0] = 0;
        }
        this.value = b;
        this.dataType = ExtraTypeConstants.EXTRA_TYPE_BOOLEAN;
    }

    public void setValueFile(String fileName) {
        FileInputStream fi = null;
        try {
            File f = new File(fileName);
            fi = new FileInputStream(f);

            ByteArrayOutputStream b = new ByteArrayOutputStream();

            int nread;
            byte[] data = new byte[1024];
            while ((nread = fi.read(data)) != -1) {
                b.write(data, 0, nread);
            }
            this.value = b.toByteArray();

        } catch (IOException ex) {
            Logger.getLogger(WSExtra.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fi.close();
            } catch (IOException ex) {
                Logger.getLogger(WSExtra.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public byte[] getValueImageFile() {
        return this.value;
    }
    
    public void setValueImageFile(String fileName) {
         this.setValueFile(fileName);
         this.dataType = ExtraTypeConstants.EXTRA_TYPE_IMAGE;
    }

    public String getMergeFn() {
        return mergeFn;
    }

    public void setMergeFn(String mergeFn) {
        this.mergeFn = mergeFn;
    }

    public void toBson(JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("tag");
        gen.writeString(tag);
        gen.writeFieldName("datatype");
        gen.writeNumber(this.dataType);
        gen.writeFieldName("schematype");
        gen.writeNumber(this.schemaType);
        gen.writeFieldName("mergefn");
        gen.writeString(this.mergeFn);
        if (value != null) {
            gen.writeFieldName("value");
            gen.writeBinary(value);
        }
        if (this.groupID != null) {
            gen.writeFieldName("gid");
            gen.writeNumber(this.groupID);
        }
        gen.writeEndObject();
    }
    
    public void toJSON(JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("tag", tag);
        gen.writeNumberField("datatype", this.dataType);
        gen.writeNumberField("schematype", this.schemaType);
        gen.writeStringField("mergefn", this.mergeFn);
        if (this.groupID != null) {
            gen.writeNumberField("gid", this.groupID);
        }
        switch(this.dataType) {
            case ExtraTypeConstants.EXTRA_TYPE_IMAGE:
                if (value == null) {
                    gen.writeStringField("value", "");
                } else {
                    gen.writeStringField("value", Base64.encodeBase64String(value));
                }
                break;
            case ExtraTypeConstants.EXTRA_TYPE_BOOLEAN:
                if (value[0] == 1) {
                    gen.writeBooleanField("value", true);
                } else {
                    gen.writeBooleanField("value", false);
                }
                break;
            default:
                if (value == null) {
                    gen.writeStringField("value", "");
                } else {
                    String valString = new String(value);
                    gen.writeStringField("value", valString);
                }
                break;
        }
        gen.writeEndObject();
    }

    public static WSExtra fromBson(JsonParser parser) throws IOException {
        String tag = null;
        byte[] value = null;
        int datatype = ExtraTypeConstants.EXTRA_TYPE_STRING;
        int schematype = ExtraTypeConstants.EXTRA_SCHEMA_ATTRIBUTE;
        Long gid = null;
        String mergeFn = null;

        // Input should be pype.ointing to START_OBJECT token.
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = parser.getCurrentName();
            parser.nextToken();
            switch (fieldname) {
                case "tag":
                    tag = parser.getText();
                    break;
                case "value":
                    value = (byte[]) parser.getEmbeddedObject();
                    break;
                case "datatype":
                    datatype = parser.getIntValue();
                    break;
                case "gid":
                    gid = parser.getLongValue();
                    break;
                case "schematype":
                    schematype = parser.getIntValue();
                    break;
                case "mergefn":
                    mergeFn = parser.getText();
                    break;
            }
        }

        return new WSExtra(tag, datatype, schematype, mergeFn, value, gid);
    }

    public void print() {
        String fmt = "TAG=''{0}'',TYPE=''{1}'',SCHEMATYPE=''{2}'',VALUE=''{3}'',GID=''{4}''";
        String val = "binary";
        switch (this.dataType) {
            case ExtraTypeConstants.EXTRA_TYPE_IMAGE:
                break;
            case ExtraTypeConstants.EXTRA_TYPE_BOOLEAN:
                if (this.value[0] == 1) {
                    val = "true";
                } else {
                    val = "false";
                }
                break;
            default:
                val = new String(this.value);
                break;
        }

        MessageFormat mf = new MessageFormat(fmt);
        System.out.println(mf.format(new Object[]{this.tag,
                    Integer.toString(this.getDataType()),
                    Integer.toString(this.getSchemaType()),
                    val,
                    (this.groupID != null) ? this.groupID : "none"
                }));
    }
}
