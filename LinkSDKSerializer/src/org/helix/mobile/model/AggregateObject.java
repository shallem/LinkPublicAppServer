/*
 * Copyright 2013 Mobile Helix, Inc.
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
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.codehaus.jackson.JsonGenerator;

/**
 * Represents an "aggregate" object, which includes a list of component objects from
 * multiple individual load commands.
 * 
 * @author shallem
 */
public class AggregateObject { //implements JSONSerializable {
    private final Map<String, Object> aggregateMap;
    
    public AggregateObject() {
        this.aggregateMap = new TreeMap<>();
    }

    public Map<String, Object> getAggregateMap() {
        return aggregateMap;
    }
    
    public void addAggregateResult(String key, Object result) {
        this.aggregateMap.put(key, result);
    }
    
//    @Override
//    public void toJSON(JsonGenerator jg) throws IOException, IllegalAccessException,
//            InvocationTargetException, NoSuchMethodException {
//         jg.writeStartObject();
//
//        /* Mark as an aggreate object for the client code. */
//        jg.writeFieldName(JSONSerializer.TYPE_FIELD_NAME);
//        jg.writeNumber(1003);
//
//        for (Map.Entry<String, Object> e : this.aggregateMap.entrySet()) {
//            if (e.getValue() == null) {
//                continue;
//            }
//            JSONSerializer.serializeObjectFields(jg, e.getValue(), new TreeSet<String>(), e.getKey());
//        }
//
//        jg.writeEndObject();       
//    }
}
