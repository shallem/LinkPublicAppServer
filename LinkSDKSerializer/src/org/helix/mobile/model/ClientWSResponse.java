/*
 * Copyright 2014 Mobile Helix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.helix.mobile.model;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

/**
 * JSON-serializable object intended for use as a response to a web services
 * call that manipulates the server side data model by updating or adding
 * objects. The returned object contains a status code, status message, and a
 * list of serializable objects (serialized according to the annotation scheme).
 * To serialize the object, the client calls the toJSON method.
 *
 * @author shallem
 */
@XmlRootElement
public class ClientWSResponse {

    private int statusCode;
    private String statusMessage;
    private List<Object> responseObjects;

    public ClientWSResponse() {
        this.statusCode = -1;
        this.statusMessage = "Uninitialized.";
        this.responseObjects = new LinkedList<>();
    }
    
    public ClientWSResponse(int statusCode,
            String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.responseObjects = new LinkedList<>();
    }

    public ClientWSResponse(int statusCode,
            String statusMessage,
            List<Object> objectList) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.responseObjects = objectList;
    }

    public void addObject(Object o) {
        if (o != null) {
           this.responseObjects.add(o);
        }
    }
    
    public void toJSON(JsonGenerator jg, JSONSerializer js)  throws IOException,
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            NoSuchMethodException {
        jg.writeStartObject();
            
        // The client reads either the return status of the return code field. Both are required
        jg.writeFieldName("status"); 
        jg.writeNumber(statusCode);
        jg.writeFieldName("code");
        jg.writeNumber(statusCode);

        jg.writeFieldName("msg");
        jg.writeString(statusMessage);
        jg.writeArrayFieldStart("objects");
        for (Object obj : this.responseObjects) {
            js.serializeObject(obj, jg);
        }
        jg.writeEndArray();
        jg.writeEndObject();
    }

    public String toJSON() throws IOException,
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            NoSuchMethodException {
        JSONSerializer js = new JSONSerializer();

        StringWriter outputString = new StringWriter();
        JsonFactory jsonF = new JsonFactory();

        try (JsonGenerator jg = jsonF.createJsonGenerator(outputString)) {
            this.toJSON(jg, js);
        }

        outputString.flush();

        return outputString.toString();
    }

    public int getCode() {
        return this.getStatusCode();
    }
    
    public int getStatusCode() {
        return statusCode;
    }

    public String getMsg() {
        return this.getStatusMessage();
    }
    
    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public void setCode(int statusCode) {
        this.setStatusCode(statusCode);
    }

    public void setMsg(String msg) {
        this.setStatusMessage(statusMessage);
    }
    
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    public List<Object> getObjects() {
        return this.responseObjects;
    }
    
    public void setObjects(List<Object> objs) {
        this.responseObjects = objs;
    }

    public static String SerializationFailureObject() {
        return "{ \"status\" : -4 }";
    }
}
