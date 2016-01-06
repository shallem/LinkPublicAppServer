package com.mobilehelix.services.objects;

import com.mobilehelix.services.interfaces.WSResponse;
import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author frederic
 */
public class ApplicationServerCheckResourceConfigResponse extends WSResponse {
    public static final String USERNAME_FIELD = "user";
    public static final String PASSWORD_FIELD = "password";
    
    private final Set<FileSystemInitTestResult> results;
    private final List<String> requiredFields;

    public ApplicationServerCheckResourceConfigResponse(int statusCode) {
        this.statusCode = statusCode;
        this.results = new HashSet<>();
        this.requiredFields = new LinkedList<>();
    }

    public ApplicationServerCheckResourceConfigResponse(byte[] bson) throws IOException {
        this.results = new HashSet<>();
        this.requiredFields = new LinkedList<>();
        this.fromBson(bson);
    }

    public void addResult(FileSystemInitTestResult result) {
        if (result != null) {
            this.results.add(result);
        }
    }

    public void getResults(Collection<FileSystemInitTestResult> results) {
        if (results != null) {
            results.addAll(this.results);
        }
    }
    
    public void addRequiredField(String f) {
        this.requiredFields.add(f);
    }
    
    public void addRequiredFields(String[] requiredFields) {
        this.requiredFields.addAll(Arrays.asList(requiredFields));
    }
    
    public void getRequiredFields(Collection<String> rf) {
        rf.addAll(this.requiredFields);
    }

    @Override
    public byte[] toBson() throws IOException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createJsonGenerator(baos);
        gen.writeStartObject();
        gen.writeFieldName("status");
        gen.writeNumber(this.statusCode);

        if (this.msg != null) {
            gen.writeFieldName("msg");
            gen.writeString(this.msg);
        }

        gen.writeArrayFieldStart("results");
        for (FileSystemInitTestResult r : this.results) {
            r.toBson(gen);
        }
        gen.writeEndArray();
        
        if (this.requiredFields != null && !this.requiredFields.isEmpty()) {
            gen.writeArrayFieldStart("fields");
            for (String s : this.requiredFields) {
                gen.writeString(s);
            }
            gen.writeEndArray();
        }
        
        gen.writeEndObject();
        gen.close();
        return baos.toByteArray();
    }

    @Override
    protected final void fromBson(byte[] data) throws IOException {
        BsonFactory factory = new BsonFactory();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        JsonParser parser = factory.createJsonParser(bais);
        parser.nextToken();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = parser.getCurrentName();
            parser.nextToken();
            switch (fieldname) {
                case "msg":
                    this.setMsg(parser.getText());
                    break;
                case "status":
                    this.setStatusCode(parser.getIntValue());
                    break;
                case "results":
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        FileSystemInitTestResult result = FileSystemInitTestResult.fromBson(parser);
                        this.addResult(result);
                    }
                    break;
                case "fields":
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        this.addRequiredField(parser.getText());
                    }
                    break;
            }
        }
    }

    public static class FileSystemInitTestResult {

        private final String name;
        private final String errorMessage;
        private final boolean success;

        public FileSystemInitTestResult(String name, String errMsg, boolean success) {
            this.name = name;
            this.errorMessage = errMsg;
            this.success = success;
        }

        public String getName() {
            return this.name;
        }

        public String getErrorMessage() {
            return this.errorMessage;
        }

        public boolean isSuccess() {
            return this.success;
        }

        public static FileSystemInitTestResult fromBson(JsonParser parser) throws IOException {
            String name = "";
            String errMsg = "";
            boolean success = false;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();
                // Move past the field name token.
                parser.nextToken();
                switch (fieldName) {
                    case "name":
                        name = parser.getText();
                        break;
                    case "msg":
                        errMsg = parser.getText();
                        break;
                    case "success":
                        success = parser.getBooleanValue();
                        break;
                }
            }

            return new FileSystemInitTestResult(name, errMsg, success);
        }

        public void toBson(JsonGenerator gen) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("name", this.name);
            gen.writeStringField("msg", this.errorMessage);
            gen.writeBooleanField("success", this.success);
            gen.writeEndObject();
        }
    }

}
