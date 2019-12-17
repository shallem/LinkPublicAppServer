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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an "aggregate" object, which includes a list of component objects from
 * multiple individual load commands.
 * 
 * @author shallem
 */
public class AggregateObject implements JSONSerializable {

    private static final Logger LOG = Logger.getLogger(AggregateObject.class.getName());
    
    
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
    
    @Override
    public void toJSON(JSONGenerator jg) throws IOException,
                IllegalAccessException,
                IllegalArgumentException,
                InvocationTargetException,
                NoSuchMethodException {
        jg.writeStartObject();

        /* Mark as an aggreate object for the client code. */
        jg.writeFieldName(JSONSerializer.TYPE_FIELD_NAME);
        jg.writeNumber(1003);

        for (Map.Entry<String, Object> e : this.getAggregateMap().entrySet()) {
            if (e.getValue() == null) {
                LOG.log(Level.WARNING, "Received unexpected null value in aggregate map with key {0}", e.getKey());
                continue;
            }
            JSONSerializer.serializeObjectFields(jg, e.getValue(), e.getKey());
        }

        jg.writeEndObject();       
    }

    @Override
    public String toString() {
        if (this.aggregateMap == null || this.aggregateMap.isEmpty()) {
            return "empty";
        }
        StringBuilder summary = new StringBuilder(1024);
        for (String k : this.aggregateMap.keySet()) {
            Object obj = this.aggregateMap.get(k);
            if (obj.getClass().isArray()) {
                summary.append(k).append("[");
                for (Object item : (Object[])obj) {
                    summary.append(item.toString()).append(",");
                }
                summary.append("]");
            } else {
                String objSummary = obj.toString();
                if (summary.length() > 0) {
                    summary.append(" ");
                }
                summary.append(k).append("{").append(objSummary).append("}");
            }
        }
        return summary.toString();
    }
}
