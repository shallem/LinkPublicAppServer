/*
 * Copyright 2009-2011 Prime Technology.
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

import java.util.HashMap;
import java.util.Map;

public enum EnumDataTypes {
     BOOLEAN("boolean")
    ,JAVA_LANG_BOOLEAN("java.lang.Boolean")
    ,BYTE("byte")
    ,JAVA_LANG_BYTE("java.lang.Byte")
    ,SHORT("short")
    ,JAVA_LANG_SHORT("java.lang.Short")
    ,INT("int")
    ,JAVA_LANG_INTEGER("java.lang.Integer")
    ,JAVA_LANG_AUTOMICINTEGER("java.lang.AtomicInteger")
    ,LONG("long")
    ,JAVA_LANG_LONG("java.lang.Long")
    ,JAVA_LANG_ATOMICLONG("java.lang.AtomicLong")
    ,CHAR("CHAR")
    ,FLOAT("float")
    ,JAVA_LANG_FLOAT("java.lang.Float")
    ,DOUBLE("double")
    ,JAVA_LANG_DOUBLE("java.lang.Double")
    ,JAVA_LANG_STRING("java.lang.String")
    ,JAVA_LANG_BIGINTEGER("java.lang.BigInteger")
    ,JAVA_LANG_BIGDECIMAL("java.lang.BigDecimal")
    ,UNKNOWN("Unknown")
    ;

    private final String text;
    
    private static final Map<String, EnumDataTypes> MAP = init();
    
    private static Map<String, EnumDataTypes> init() {
        Map<String, EnumDataTypes> res = new HashMap<>();
        
        for (EnumDataTypes ve : EnumDataTypes.values()) {
            res.put(ve.text.toLowerCase(), ve);
        }
        
        return res;
    }

    private EnumDataTypes(String text) {
        this.text = text;
    }
    
    public String getText() {
        return this.text;
    }

    public static EnumDataTypes getEnumFromString(String text) 
            throws IllegalArgumentException {
        EnumDataTypes res = (text == null) ? null : MAP.get(text.toLowerCase());

        if (res != null)
            return res;
        
        //return EnumDataTypes.UNKNOWN; 
        throw new IllegalArgumentException("No EnumDataType with text '" + String.valueOf(text) + "' found"); 
    }
}
