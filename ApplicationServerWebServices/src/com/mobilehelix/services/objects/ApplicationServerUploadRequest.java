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
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public class ApplicationServerUploadRequest extends WSRequest {
    private byte[] docBytes;
    private int docType;
    private int appid;
    private String docName;
    private String listUUID;
    private String docTitle;
    private String directoryPath;
    private String fileDigest;

    public static final int DOC_TYPE_DOCX = 1;
    public static final int DOC_TYPE_XLSX = 2;
    public static final int DOC_TYPE_PDF = 3;
    
    public ApplicationServerUploadRequest() {
        this.docBytes = null;
        this.docType = -1;
    }
    
    public byte[] getDocBytes() {
        return docBytes;
    }

    public void setDocBytes(byte[] docBytes) {
        this.docBytes = docBytes;
    }

    public int getDocType() {
        return docType;
    }

    public void setDocType(int docType) {
        this.docType = docType;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getListUUID() {
        return listUUID;
    }

    public void setListUUID(String listUUID) {
        this.listUUID = listUUID;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public String getFileDigest() {
        return fileDigest;
    }

    public void setFileDigest(String fileDigest) {
        this.fileDigest = fileDigest;
    }

    public int getAppid() {
        return appid;
    }

    public void setAppid(int appid) {
        this.appid = appid;
    }
    
    @Override
    public byte[] toBson() throws IOException {
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        JsonGenerator gen = factory.createJsonGenerator(baos);
        gen.writeStartObject();
        gen.writeFieldName("type");
        gen.writeNumber(this.docType);
        gen.writeFieldName("doc");
        gen.writeBinary(this.docBytes);
        if (this.docTitle == null) {
            gen.writeFieldName("title");
            gen.writeString(this.docTitle);
        }
        if (this.listUUID != null) {
            gen.writeFieldName("uuid");
            gen.writeString(this.listUUID);
        }
        if (this.directoryPath != null) {
            gen.writeFieldName("dir");
            gen.writeString(this.directoryPath);
        }
        if (this.fileDigest != null) {
            gen.writeFieldName("file");
            gen.writeString(this.fileDigest);
        }
        if (this.appid >= 0) {
            gen.writeFieldName("appid");
            gen.writeNumber(this.appid);
        }
        gen.close();
        return baos.toByteArray();
    }
    
    public static ApplicationServerUploadRequest fromBson(byte[] data) throws IOException {
        ApplicationServerUploadRequest asur = new ApplicationServerUploadRequest();
        JsonParser parser = WSResponse.InitFromBSON(data);
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            // Move past field name token.
            parser.nextToken();
            switch (fieldName) {
                case "type":
                    asur.setDocType(parser.getIntValue());
                    break;
                case "doc":
                    asur.setDocBytes((byte[])parser.getEmbeddedObject());
                    break;
                case "title":
                    asur.setDocTitle(parser.getText());
                    break;
                case "uuid":
                    asur.setListUUID(parser.getText());
                    break;
                case "dir":
                    asur.setDirectoryPath(parser.getText());
                    break;
                case "file":
                    asur.setFileDigest(parser.getText());
                    break;
                case "appid":
                    asur.setAppid(parser.getIntValue());
                    break;
            }
        }
        return asur;
    }
}
