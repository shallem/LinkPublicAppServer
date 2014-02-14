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
public class WSExtra implements Comparable {
    public enum SerializeOptions {
        DEVICE_ONLY,
        EXCLUDE_DEVICE,
        INCLUDE_ALL
    };

    public String tag;
    public int dataType;
    public int schemaType;
    public String mergeFn;
    public Long groupID;
    public byte[] value;
    public Boolean isToDevice;

    public WSExtra() {
        this.mergeFn = "default";
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
    
    public void setValueBoolean(boolean b) {
        byte[] val = new byte[1];
        if (b) {
            val[0] = 1;
        } else {
            val[0] = 0;
        }
        this.value = val;
        this.dataType = ExtraTypeConstants.EXTRA_TYPE_BOOLEAN;
    }
    
    public void setValueBooleanString(String s) {
        byte[] b = new byte[1];
        if (s.equals("true") ||
                s.equals("True") ||
                s.equals("t")) {
            this.setValueBoolean(true);
        } else {
            this.setValueBoolean(false);
        }
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
    
    public Integer getValueInteger() {
        String s = new String(this.value);
        return Integer.parseInt(s);
    }
    
    public void setValueInteger(Integer i) throws UnsupportedEncodingException {
        this.value = i.toString().getBytes("US-ASCII");
        this.dataType = ExtraTypeConstants.EXTRA_TYPE_INT;
    }

    public String getMergeFn() {
        return mergeFn;
    }

    public void setMergeFn(String mergeFn) {
        this.mergeFn = mergeFn;
    }

    public boolean isIsToDevice() {
        return isToDevice;
    }

    public void setIsToDevice(boolean isToDevice) {
        this.isToDevice = isToDevice;
    }

    public void toBson(JsonGenerator gen, SerializeOptions serializeOptions) throws IOException {
        if (this.isToDevice == null) {
            throw new IOException("Must set the toDevice field of a WSExtra to serialize it.");
        }
        switch(serializeOptions) {
            case DEVICE_ONLY:
                if (!this.isToDevice) {
                    return;
                }
                break;
            case EXCLUDE_DEVICE:
                if (this.isToDevice) {
                    return;
                }
                break;
            case INCLUDE_ALL:
                break;
        }
        
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
    
    public void toJSON(JsonGenerator gen, SerializeOptions serializeOptions) throws IOException {
        if (this.isToDevice == null) {
            throw new IOException("Must set the toDevice field of a WSExtra to serialize it.");
        }
        switch(serializeOptions) {
            case DEVICE_ONLY:
                if (!this.isToDevice) {
                    return;
                }
                break;
            case EXCLUDE_DEVICE:
                if (this.isToDevice) {
                    return;
                }
                break;
            case INCLUDE_ALL:
                break;
        }
        
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
        WSExtra ex = new WSExtra();
        
        // Input should be pype.ointing to START_OBJECT token.
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = parser.getCurrentName();
            parser.nextToken();
            switch (fieldname) {
                case "tag":
                    ex.setTag(parser.getText());
                    break;
                case "value":
                    ex.setValue((byte[]) parser.getEmbeddedObject());
                    break;
                case "datatype":
                    ex.setDataType(parser.getIntValue());
                    break;
                case "gid":
                    ex.setGroupID(parser.getLongValue());
                    break;
                case "schematype":
                    ex.setSchemaType(parser.getIntValue());
                    break;
                case "mergefn":
                    ex.setMergeFn(parser.getText());
                    break;
            }
        }

        return ex;
    }

    @Override
    public int compareTo(Object o) {
        WSExtra oExtra = (WSExtra)o;
        return this.tag.compareTo(oExtra.tag);
    }
}
