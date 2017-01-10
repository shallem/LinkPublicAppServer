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
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;


/**
 * Accept as input a class that represents a JSON schema to be transmitted to
 * the client, primarily via the pm:loadCommand JSF tag. Translate this into a
 * generic JSON description of the corresponding schema. The schema differs from
 * actual data in that we are trying to show what is potentially included in a
 * returned data object, not what is returned in any particular load command. If
 * we don't explore the space of what's possible, our schema on the client side
 * would change every time particular fields are or are not present in the data
 * model.
 *
 * The resulting schema is a JSON string that fills in default values for all
 * primitive types, makes all arrays singleton arrays containing a specification
 * of the underlying object type, and makes all referenced objects into JSON
 * specifications of the schema.
 *
 * Object fields that are included in the client-side schema must be marked with
 * the
 *
 * @ClientData annotation. To avoid infinite loops, we only attempt to serialize
 * the schema for each class once.
 *
 * @author shallem
 */
public class JSONSerializer {
    private static final Logger LOG = Logger.getLogger(JSONSerializer.class.getName());

    public static final String TYPE_FIELD_NAME = "__hx_type";
    public static final String SCHEMA_TYPE_FIELD_NAME = "__hx_schema_type";
    public static final String SCHEMA_NAME_FIELD_NAME = "__hx_schema_name";
    public static final String KEY_FIELD_NAME = "__hx_key";
    public static final String SORTS_FIELD_NAME = "__hx_sorts";
    public static final String FILTERS_FIELD_NAME = "__hx_filters";
    public static final String GLOBAL_FILTERS_FIELD_NAME = "__hx_global_filters";
    public static final String TEXT_INDEX_FIELD_NAME = "__hx_text_index";
    
    private static final HashMap<String, Boolean> HAS_CLIENT_DATA_CACHE = new HashMap<>();
    private static final HashMap<String, Boolean> HAS_CLIENT_METHOD_DATA_CACHE = new HashMap<>();
    
    
    public JSONSerializer() {
    }

    public static String serializeError(String msg) {
        try {
            StringWriter outputString = new StringWriter();
            JsonFactory jsonF = new JsonFactory();
            JsonGenerator gen = jsonF.createJsonGenerator(outputString);
            gen.writeStartObject();
            gen.writeStringField("error", msg);
            gen.writeEndObject();
            
            JSONGenerator jg = new JSONGenerator(gen, new TreeSet<String>());
            outputString.flush();
            
            return outputString.toString();
        } catch (IOException ex) {
            Logger.getLogger(JSONSerializer.class.getName()).log(Level.SEVERE, "Failed to serialize JSON error.", ex);
            return "{ 'error' : 'Serialization of the error message failed. Please review the server logs.' }";
        }
    }
    
    public static String serializeObject(Object obj) throws IOException,
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            NoSuchMethodException {
        // Make this big initially. The memory cost is low compared to the cost of copying this string when the underlying buffer must be resized.
        StringWriter outputString = new StringWriter(256 * 1024);
        JsonFactory jsonF = new JsonFactory();
        JsonGenerator gen = jsonF.createJsonGenerator(outputString);
        JSONGenerator jg = new JSONGenerator(gen, new TreeSet<String>());
        serializeObject(obj, jg);      
        outputString.flush();        
        return outputString.toString();
    }
    
    public static void serializeObject(Object obj,
            JSONGenerator jg) throws IOException,
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            NoSuchMethodException {
        serializeObjectFields(jg, obj, null);
    }

    private static void iterateOverObjectField(JSONGenerator jg,
            Object obj,
            Method getter) throws IllegalAccessException, IllegalArgumentException, 
                                  InvocationTargetException, IOException, NoSuchMethodException {
        /* Extract the field name. */
        String nxtFieldName = extractFieldName(getter.getName());
        LOG.log(Level.FINEST, "Serializing {0}", nxtFieldName);

        /* Finally, handle arbitrary object types. Either these objects
         * encapsulate other objects (as evidenced by having ClientData-
         * annotated methods or they have a toString
         */       
        Object subObj = (Object) getter.invoke(obj, new Object[]{});
        if (subObj != null && !serializeObjectFields(jg, subObj, nxtFieldName)) {
            /* Should never happen. */
            throw new IOException("Serialization unexpectedly encountered a class with no ClientData: " + 
                    subObj.getClass().getName());
        }
    }
    
    public static boolean serializeObjectFields(JSONGenerator jg,
            Object obj,
            String fieldName) throws IOException, IllegalAccessException, 
                                     IllegalArgumentException, InvocationTargetException, 
                                     NoSuchMethodException {
        try {
            Class<?> c = obj.getClass();

            if (isSimpleType(c)) {
                if (fieldName != null) {
                    jg.writeFieldName(fieldName);
                }
                
                addSimpleData(jg, obj);
                return true;
            }
            
            if (c.isArray()) {
                if (fieldName != null) {
                    jg.writeArrayFieldStart(fieldName);
                } else {
                    jg.writeStartArray();
                }
                for (Object elem : (Object[]) obj) {
                    serializeObjectFields(jg, elem, null);
                }
                jg.writeEndArray();
                return true;
            } 
                
            /* Next, iterate over all methods looking for property getters, of the form
             * get<prop name>. Find those annotated with the ClientData annotation. Presuming
             * the name format is right, convert the method name to a field name and add
             * to the schema. Throw an IO exception is an annotated method has the wrong
             * name format.
             */
            if (fieldName != null) {
                jg.writeFieldName(fieldName);
            }
            
            // Can we delegate the serialization to specialized code ?
            if (obj instanceof JSONSerializable) {
                ((JSONSerializable) obj).toJSON(jg);
                return true;
            }           
            
            if (isDeltaObject(c)) {
                jg.writeStartObject();

                // Mark as a delta object for the client code
                jg.writeFieldName(TYPE_FIELD_NAME);
                jg.writeNumber(1001);

                Method m = c.getMethod("getAdds", (Class[]) null);
                Class<?> returnType = m.getReturnType();
                jg.writeFieldName(SCHEMA_TYPE_FIELD_NAME);
                jg.writeString(returnType.getComponentType().getName());
                iterateOverObjectField(jg, obj, m);

                m = c.getMethod("getDeletes", (Class[]) null);
                iterateOverObjectField(jg, obj, m);

                m = c.getMethod("getUpdates", (Class[]) null);
                iterateOverObjectField(jg, obj, m);

                m = c.getMethod("getDeleteSpec", (Class[]) null);
                Criteria[] deleteSpec = (Criteria[])m.invoke(obj, new Object[]{});
                if (deleteSpec != null) {
                    jg.writeArrayFieldStart("deleteSpec");
                    for (Criteria crit : deleteSpec) {
                        if (crit != null) {
                            crit.toJSON(jg);
                        }
                    }
                    jg.writeEndArray();
                }

                jg.writeEndObject();
                return true;
            } 
            
            if (isAggregateObject(c)) {
                jg.writeStartObject();

                /* Mark as an aggreate object for the client code. */
                jg.writeFieldName(TYPE_FIELD_NAME);
                jg.writeNumber(1003);

                AggregateObject a = (AggregateObject)obj;
                for (Map.Entry<String, Object> e : a.getAggregateMap().entrySet()) {
                    if (e.getValue() == null) {
                        LOG.log(Level.WARNING, "Received unexpected null value in aggregate map with key {0}", e.getKey());
                        continue;
                    }
                    serializeObjectFields(jg, e.getValue(), e.getKey());
                }

                jg.writeEndObject();
                return true;
            } 
            
            if (isParamObject(c)) {
                jg.writeStartObject();

                /* Mark as a param object for the client code. */
                jg.writeNumberField(TYPE_FIELD_NAME, 1004);

                /* Write the param. */
                ParamObject po = (ParamObject)obj;
                if (po.getParamObject() != null) {
                    serializeObjectFields(jg, po.getParamObject(), "param");
                }
                if (po.getSyncObject() != null) {
                    serializeObjectFields(jg, po.getSyncObject(), "sync");
                }

                jg.writeEndObject();
                return true;
            }
            
            if (isErrorObject(c)) {
                ClientWSResponse errObj = (ClientWSResponse)obj;
                jg.writeStartObject();
                jg.writeFieldName("error");
                errObj.toJSON(jg.getDelegate());
                jg.writeEndObject();
                return true;
            } 
            
            if (hasClientDataMethods(c)) {               
                jg.writeStartObject();

                /* Write the object type so that we can get the Schema back. */
                jg.writeFieldName(SCHEMA_TYPE_FIELD_NAME);
                jg.writeString(c.getName());

                for (Method m : c.getMethods()) {
                    String key = getFullyQualifiedName(c, m);
                    if (HAS_CLIENT_METHOD_DATA_CACHE.containsKey(key)) {
                        iterateOverObjectField(jg, obj, m);
                        continue;
                    }

                    Annotation clientDataAnnot = m.getAnnotation(ClientData.class);
                    if (clientDataAnnot != null) {
                        iterateOverObjectField(jg, obj, m);
                        HAS_CLIENT_METHOD_DATA_CACHE.put(key, true);
                    }
                }
                jg.writeEndObject();
                return true;
            } else {
                throw new IOException("Cannot serialize an object with no ClientData fields in " +
                        obj.getClass().getName());
            }
        } catch(Exception e) {
            LOG.log(Level.SEVERE, "Failed to serialize field {0}", fieldName);
            throw e;
        }
    }
    
    public static String getFullyQualifiedName(Class<?> c, Method m) {
        return c.getName() + "." + m.getName();
    }

    public String serializeObjectSchema(Class<?> cls) throws IOException {
        TreeSet<String> visitedClasses = new TreeSet<>();
        StringWriter outputString = new StringWriter();

        JsonGenerator gen = new JsonFactory().createJsonGenerator(outputString);
        JSONGenerator jg = new JSONGenerator(gen, visitedClasses);

        if (!serializeObjectForSchema(jg, cls, null, null)) {
            throw new IOException("Attempting to generate schema for an object with no client data in "
                    + cls.getName());
        }

        outputString.flush();
        return outputString.getBuffer().toString();
    }

    private static boolean serializeObjectForSchema(JSONGenerator jg,
            Class<?> c,
            String fieldName,
            String alternateName) throws IOException {
        if ("id".equals(fieldName)) {
            throw new IOException("Class " + c.getName() + " uses the field name 'id', " +
                    "which is reserved for use by PersistenceJS.");
        }
        
        /* Serialize the genericized version of this return type. */
        if (isSimpleType(c)) {
            if (fieldName != null) {
                jg.writeFieldName(fieldName);
            }
            addSimpleType(jg, c);
            return true;
        }
        
        if (c.isArray()) {
            /* Handle arrays by recursing over the element type. */
            Class<?> componentType = c.getComponentType();
            if (fieldName != null) {
                jg.writeArrayFieldStart(fieldName);
            } else {
                jg.writeStartArray();
            }
            if (isSimpleType(componentType)) {
                throw new IOException("Arrays of simple types (e.g., strings, ints, " +
                        "etc.) are currently not supported. Wrap your String in a class " +
                        "of its own and add a ClientData getter so that the wrapper " +
                        "class can become its own table on the client. Class: " + c.getName() + 
                        ", field: " + fieldName);
            }
            
            if (!serializeObjectForSchema(jg,
                    componentType,
                    null,
                    alternateName)) {
                throw new IOException("Array types returned by ClientData methods " +
                        "must be object types with at least one ClientData field. " +
                        "Class " + componentType.getName() + " does not comply.");
            }
            jg.writeEndArray();
            return true;
        }

        /* Check is this is a delta object. If so, just iterate over the object type of the
         * getAdds method.
         */
        if (isDeltaObject(c)) {
            try {
                Method m = c.getMethod("getAdds", (Class[]) null);
                if (!serializeObjectForSchema(jg, m.getReturnType(), 
                       fieldName, alternateName)) {
                    /* The object neither has any fields marked as ClientData nor
                     * does it have a toString method - this is not legal.
                    */
                    throw new IOException("Object types must either have fields " +
                            "marked ClientData or have a toString method in class " + 
                            c.getName());
                }
                return true;
            } catch (NoSuchMethodException ex) {
                throw new IOException("Invalid contents of DeltaObject. " +
                        "Missing getAdds method in class " + c.getName());
            } catch (SecurityException  ex) {
                throw new IOException("Invalid contents of DeltaObject. " + ""
                        + "Missing getAdds method in class " + c.getName());
            }
        } 
        
        if (isParamObject(c)) {
            try {
                Method m = c.getMethod("getSyncObject", (Class[]) null);
                if (!serializeObjectForSchema(jg, m.getReturnType(), 
                        fieldName, alternateName)) {
                    /* The object neither has any fields marked as ClientData nor
                     * does it have a toString method - this is not legal.
                    */
                    throw new IOException("Object types must either have fields " +
                            "marked ClientData or have a toString method in class " + 
                            c.getName());
                }
                return true;
            } catch (NoSuchMethodException ex) {
                throw new IOException("Invalid contents of ParamObject. " +
                        "Missing getParamObject method in class " + c.getName());
            } catch (SecurityException  ex) {
                throw new IOException("Invalid contents of ParamObject. " + ""
                        + "Missing getParamObject method in class " + c.getName());
            }
        }

        /* Finally, handle arbitrary object types. Either these objects
         * encapsulate other objects (as evidenced by having ClientData-
         * annotated methods or they have a toString
         */

        /* Next, iterate over all methods looking for property getters, of the form
         * get<prop name>. Find those annotated with the ClientData annotation. Presuming
         * the name format is right, convert the method name to a field name and add
         * to the schema by recursing. Throw an IO exception is an annotated method has the wrong
         * name format.
         */
        if (hasClientDataMethods(c)) {
            if (fieldName != null) {
                /* This is an object that will exist in its own table on the client side. */
                jg.writeFieldName(fieldName);
            }

            jg.writeStartObject();
            jg.writeFieldName(SCHEMA_NAME_FIELD_NAME);
            
            if (alternateName == null) {
                jg.writeString(c.getName());
            } else {
                jg.writeString(alternateName);
            }

            /* Prevent infinite loops. If we have already visited this object then
             * we have already defined its schema. Just return true. However, we do 
             * need to put in a reference to the master schema so that the client
             * knows that there is a schema relationship here.
             */
            if (jg.getVisitedClasses().contains(c.getCanonicalName())) {
                // Indicate that this is, essentially, a forward ref.
                jg.writeFieldName(SCHEMA_TYPE_FIELD_NAME);
                jg.writeNumber(1002);
                jg.writeEndObject();
                return true;
            }
            jg.addVisitedClass(c.getCanonicalName());
            serializeClass(jg, c);
            jg.writeEndObject(); 
            return true;
        }  else {
            throw new IOException("Class must have at least one field annotated as a ClientData field: " + c.getName());
        }
    }
    
    
    private static void serializeClass(JSONGenerator jg, Class c) throws IOException {
        String keyField = null;
        
        // Lazy instantiation of collections
        Map<String, ClientSort> sortFields = null;
        Map<String, String> filterFields = null;
        List<String> indexFields = null;
        Map<String, GlobalFilterField> globalFilterFields = null;

        for (Method m : c.getMethods()) {
            if (!Modifier.isPublic(m.getModifiers()))
                continue;
            if (Modifier.isNative(m.getModifiers()))
                continue;

            Annotation clientDataAnnot = m.getAnnotation(org.helix.mobile.model.ClientData.class);
            if (clientDataAnnot != null) {
                /* Check the method name. Throws an IOException if the name is ill-formed. */
                String methodName = m.getName();
                checkMethodName(methodName);

                /* Extract the field name. */
                String nxtFieldName = extractFieldName(methodName);

                /* Determine if this field is a sort field. */
                Annotation sortAnnot =
                        m.getAnnotation(ClientSort.class);
                if (sortAnnot != null) {
                    if (sortFields == null) {
                        sortFields = new TreeMap<>();
                    }
                    ClientSort cSortAnnot = (ClientSort)sortAnnot;
                    sortFields.put(nxtFieldName, cSortAnnot);
                }

                /* Determine if this field is a filter field. */
                Annotation filterAnnot = m.getAnnotation(ClientFilter.class);
                if (filterAnnot != null) {
                    if (filterFields == null) {
                        filterFields = new TreeMap<>();
                    }
                    ClientFilter cFilterAnnot = (ClientFilter)filterAnnot;
                    filterFields.put(nxtFieldName, cFilterAnnot.displayName());
                }

                /* Determin if this field is a global filter field. */
                Annotation globalFilterAnnot = m.getAnnotation(ClientGlobalFilter.class);
                if (globalFilterAnnot != null) {
                    ClientGlobalFilter cFilterAnnot = (ClientGlobalFilter)globalFilterAnnot;

                    if (globalFilterFields == null) {
                        globalFilterFields = new TreeMap<>();
                    }
                    globalFilterFields.put(nxtFieldName, new GlobalFilterField(cFilterAnnot.displayName(),
                            cFilterAnnot.intValues(),
                            cFilterAnnot.values(),
                            cFilterAnnot.valueNames()
                            ));
                }

                /* Determine if this field is a key field. */
                Annotation keyAnnot = m.getAnnotation(ClientDataKey.class);
                if (keyAnnot != null) {
                    if (keyField != null) {
                        throw new IOException("Client data can only have one field annotated as a ClientDataKey.");
                    }
                    keyField = nxtFieldName;
                }

                /* Determine if this field is an indexed field. */
                Annotation indexedAnnot = m.getAnnotation(ClientTextIndex.class);
                if (indexedAnnot != null) {
                    if (indexFields == null) {
                        indexFields = new ArrayList<>();
                    }
                    indexFields.add(nxtFieldName);
                }

                /* See if there is an annotation indicating a table name other than the class name. */
                Annotation clientTableAnnot = m.getAnnotation(ClientTableName.class);
                String altName = null;
                if (clientTableAnnot != null) {
                    altName = ((ClientTableName)clientTableAnnot).tableName();
                }

                /* Recurse over the method. */
                if (!serializeObjectForSchema(jg, m.getReturnType(), nxtFieldName, altName)) {
                    /* The object neither has any fields marked as ClientData nor
                     * does it have a toString method - this is not legal.
                    */
                    throw new IOException("Object types must either have fields marked ClientData or have a toString method.");
                }
            }
        }

        /* Store the keys and sort fields in the object schema. */
        if (keyField == null) {
            throw new IOException("Client data must have at least one field annotated as a ClientDataKey: " + c.getName());
        }

        jg.writeFieldName(KEY_FIELD_NAME);
        jg.writeString(keyField);
        jg.writeObjectFieldStart(SORTS_FIELD_NAME);

        if (sortFields != null) {
            for (Entry<String, ClientSort> e : sortFields.entrySet()) {
                jg.writeFieldName(e.getKey());
                jg.writeStartObject();
                jg.writeStringField("display", e.getValue().displayName());
                jg.writeStringField("direction", e.getValue().defaultOrder());
                jg.writeStringField("usecase", e.getValue().caseSensitive());
                jg.writeEndObject();
            }
        }
        jg.writeEndObject();

        jg.writeObjectFieldStart(FILTERS_FIELD_NAME);

        if (filterFields != null) {
            for(Entry<String, String> e: filterFields.entrySet()) {
                jg.writeFieldName(e.getKey());
                jg.writeString(e.getValue());
            }
        }
        jg.writeEndObject();

        jg.writeObjectFieldStart(GLOBAL_FILTERS_FIELD_NAME);

        if (globalFilterFields != null) {
            for(Entry<String, GlobalFilterField> ge : globalFilterFields.entrySet()) {
                jg.writeFieldName(ge.getKey());
                ge.getValue().serialize(jg);
            }
        }
        jg.writeEndObject();

        jg.writeArrayFieldStart(TEXT_INDEX_FIELD_NAME);

        if (indexFields != null) {
            for (String s : indexFields) {
                jg.writeString(s);
            }
        }
        jg.writeEndArray();      
    }

    public static void checkMethodName(String methodName) throws IOException {
        /* Check the format of the method name. */
        if (methodName.startsWith("get")) {
            if (methodName.length() < 4)
                throw new IOException("All getters annotated with the ClientData annotation should have the form get<field name>: "  + methodName);
        } else if (methodName.startsWith("is")) {
            if (methodName.length() < 3)
                throw new IOException("All getters annotated with the ClientData annotation should have the form is<field name>: "  + methodName);
        } else {
            throw new IOException("All getters annotated with the ClientData annotation should have the form get/is<field name>: " + methodName);
        }
    }
    
    private static boolean isNumberType(Class<?> returnType) {     
        return Number.class.equals(returnType.getSuperclass());
    }

    private static boolean isString(Class<?> returnType) {
        return String.class.equals(returnType);
    }

    private static boolean isBoolean(Class<?> returnType) {
        return Boolean.class.equals(returnType);
    }

    public static boolean isSimpleType(Class<?> objType) {
        return objType.isPrimitive()
                || isNumberType(objType)
                || isString(objType)
                || isBoolean(objType);
    }

    public static void addSimpleData(JSONGenerator jg, Object obj)
            throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        EnumDataTypes dtc;
        try {
            dtc = EnumDataTypes.getEnumFromString(obj.getClass().getName());
        } catch (IllegalArgumentException iae) {
            // Data type unknown
            dtc = EnumDataTypes.UNKNOWN;
        }
        switch (dtc) {
            case BOOLEAN:
            case JAVA_LANG_BOOLEAN:
                jg.writeBoolean((Boolean) obj);
                break;
            case BYTE:
            case JAVA_LANG_BYTE:
                jg.writeNumber((Byte) obj);
                break;
            case JAVA_LANG_SHORT:
            case SHORT:
                jg.writeNumber((Short) obj);
                break;
            case JAVA_LANG_INTEGER:
            case JAVA_LANG_AUTOMICINTEGER:
            case INT:
                jg.writeNumber((Integer) obj);
                break;
            case LONG:
            case JAVA_LANG_LONG:
            case JAVA_LANG_ATOMICLONG:
                jg.writeNumber((Long) obj);
                break;
            case CHAR:
                jg.writeRaw('a'); // ???
                break;
            case FLOAT :
            case JAVA_LANG_FLOAT:
                jg.writeNumber((Float) obj);
            case DOUBLE:
            case JAVA_LANG_DOUBLE:
                jg.writeNumber((Double) obj);
                break;
            case JAVA_LANG_STRING:
                jg.writeString((String) obj);
                break;
            case JAVA_LANG_BIGINTEGER:
                jg.writeNumber((BigInteger) obj);
                break;
            case JAVA_LANG_BIGDECIMAL:
                jg.writeNumber((BigDecimal) obj);
                break;
        }
    }

    private static void addSimpleType(JSONGenerator jg, Class<?> objType) throws IOException {
        EnumDataTypes dtc;
        try {
            dtc = EnumDataTypes.getEnumFromString(objType.getName());
        } catch (IllegalArgumentException iae) {
            dtc = EnumDataTypes.UNKNOWN; // TODO: write error message ?
        }
        switch (dtc) {
            case BOOLEAN:
            case JAVA_LANG_BOOLEAN:
                jg.writeBoolean(true);
                break;
            case BYTE:
            case JAVA_LANG_BYTE:
                jg.writeNumber((int) 1);
                break;
            case SHORT:
            case JAVA_LANG_SHORT:
                jg.writeNumber((int) 1);
                break;
            case JAVA_LANG_INTEGER:
            case JAVA_LANG_AUTOMICINTEGER:
            case INT:
                jg.writeNumber((int) 1);
                break;
            case JAVA_LANG_LONG:
            case JAVA_LANG_ATOMICLONG:
            case LONG:
                jg.writeNumber((long) 1);
                break;
            case CHAR:
                jg.writeRaw('a'); // ???
                break;
            case FLOAT:
            case JAVA_LANG_FLOAT:
                jg.writeNumber((float) 1.0);
            case DOUBLE:
            case JAVA_LANG_DOUBLE:
                jg.writeNumber((double) 1.0);
                break;
            case JAVA_LANG_STRING:
                jg.writeString("empty");
                break;
            case JAVA_LANG_BIGINTEGER:
            case JAVA_LANG_BIGDECIMAL:
                jg.writeNumber(BigInteger.ONE);
                break;
        }
    }

    private static boolean hasClientDataMethods(Class<?> c) {
        String cName = c.getName();
        if (cName != null &&
                HAS_CLIENT_DATA_CACHE.containsKey(cName)) {
            return HAS_CLIENT_DATA_CACHE.get(cName);
        }

        if (c.getAnnotation(ClientClass.class) != null) {
            if (cName != null) {
                HAS_CLIENT_DATA_CACHE.put(cName, true);
            }
            return true;
        }
            
        for (Method m : c.getMethods()) {
            if (!Modifier.isPublic(m.getModifiers()))
                continue;
            if (Modifier.isNative(m.getModifiers()))
                continue;
            if (m.getAnnotation(ClientData.class) != null) {
                if (cName != null) {
                    LOG.log(Level.WARNING, "Class {0} has a client method [{1}] but "+ 
                            "lacks the class level annotation", new Object[]{c, m.getName()});
                    HAS_CLIENT_DATA_CACHE.put(cName, true);
                }
                return true;
            }
        }
        if (cName != null) {
            HAS_CLIENT_DATA_CACHE.put(cName, false);
        }
        return false;
    }


    private static boolean isDeltaObject(Class<?> c) {
        return DeltaObject.class.isAssignableFrom(c);
    }
    
    private static boolean isAggregateObject(Class<?> c) {
        return c.equals(AggregateObject.class);
    }
    
    private static boolean isParamObject(Class<?> c) {
        return ParamObject.class.isAssignableFrom(c);
    }
    
    private static boolean isErrorObject(Class<?> c) {
        return c.equals(ClientWSResponse.class);
    }
    
    public static String extractFieldName(String methodName) {
        int startIdx = methodName.startsWith("get") ? 3 : 2;
        return Character.toLowerCase(methodName.charAt(startIdx)) +
                 methodName.substring(startIdx+1);
    }

}
