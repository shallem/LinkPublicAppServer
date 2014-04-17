/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package com.mobilehelix.services.objects;

import com.mobilehelix.services.interfaces.WSRequest;
import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public class WSAdminCommand {
    private String commandName;
    private String[] commandArgs;
    
    public WSAdminCommand() {
        
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String[] getCommandArgs() {
        return commandArgs;
    }

    public void setCommandArgs(String[] commandArgs) {
        this.commandArgs = commandArgs;
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
        gen.writeStringField("cmd", commandName);
        if (commandArgs != null) {
            gen.writeArrayFieldStart("args");
            for (String arg : commandArgs) {
                gen.writeString(arg);
            }
            gen.writeEndArray();
        }
	gen.writeEndObject();
    }

    public static WSAdminCommand fromBson(byte[] data) throws IOException {
        JsonParser parser = WSRequest.InitFromBSON(data);
        return WSAdminCommand.fromBson(parser);
    }
    
    public static WSAdminCommand fromBson(JsonParser parser) throws IOException {
	String cmdName = null;
        List<String> args = null;
        
        // Input should be pointing to START_OBJECT token.
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = parser.getCurrentName();
            parser.nextToken();
            switch (fieldname) {
                case "cmd":
                    cmdName = parser.getText();
                    break;
                case "args":
                    args = new LinkedList<>();
                    // Advance past start array.
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        // Should be pointing to START_OBJECT
                        String s = parser.getText();
                        args.add(s);
                    }
                    break;
            }
        }
        
        WSAdminCommand adminCmd = new WSAdminCommand();
	adminCmd.setCommandName(cmdName);
        
        String[] arr = new String[args.size()];
        adminCmd.setCommandArgs(args.toArray(arr));
        
        return adminCmd;
    }
}
