package org.helix.mobile.model;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

/**
 * Decorator for JsonGenerator with an extra state 'visitedClasses'
 * Because the extra state is encapsulated JSONGenerator can be used
 * in place of org.codehaus.jackson.JsonGenerator in JSONSerializer
 * and JSONSerializable interface.
 * @author frederic
 */
public class JSONGenerator implements Closeable {
    
    private final JsonGenerator delegate;
    private final Set<String> visitedClasses;
    
    
    public JSONGenerator(JsonGenerator delegate, Set<String> visitedClasses) {
        this.delegate = delegate;
        this.visitedClasses = visitedClasses;
    }

    public JsonGenerator getDelegate() {
        return delegate;
    }

    public void addVisitedClass(String cl) {
        this.visitedClasses.add(cl);
    }
     
     public Set<String> getVisitedClasses() {
        return this.visitedClasses;
    }

    public void setSchema(FormatSchema schema) {
        this.delegate.setSchema(schema);
    }

    public boolean canUseSchema(FormatSchema schema) {
        return this.delegate.canUseSchema(schema);
    }

    public Version version() {
        return this.delegate.version();
    }

    public Object getOutputTarget() {
        return this.delegate.getOutputTarget();
    }


    public JsonGenerator configure(JsonGenerator.Feature ftr, boolean bln) {
        return this.delegate.configure(ftr, bln);
    }

    public boolean isEnabled(JsonGenerator.Feature ftr) {
        return this.delegate.isEnabled(ftr);
    }

    public JsonGenerator setCodec(ObjectCodec oc) {
        return this.delegate.setCodec(oc);
    }

    public ObjectCodec getCodec() {
        return this.delegate.getCodec();
    }

    public JsonGenerator setPrettyPrinter(PrettyPrinter pp) {
        return this.delegate.setPrettyPrinter(pp);
    }

    public JsonGenerator useDefaultPrettyPrinter() {
        return this.delegate.useDefaultPrettyPrinter();
    }

    public JsonGenerator setHighestNonEscapedChar(int i) {
        return this.delegate.setHighestNonEscapedChar(i);
    }

    public int getHighestEscapedChar() {
        return this.delegate.getHighestEscapedChar();
    }

    public CharacterEscapes getCharacterEscapes() {
        return this.delegate.getCharacterEscapes();
    }

    public JsonGenerator setCharacterEscapes(CharacterEscapes ce) {
        return this.delegate.setCharacterEscapes(ce);
    }

    public void writeStartArray() throws IOException, JsonGenerationException {
       this.delegate.writeStartArray();
    }

    public void writeEndArray() throws IOException, JsonGenerationException {
       this.delegate.writeEndArray();
    }

    public void writeStartObject() throws IOException, JsonGenerationException {
       this.delegate.writeStartObject();
    }

    public void writeEndObject() throws IOException, JsonGenerationException {
        this.delegate.writeEndObject();
    }

    public void writeFieldName(String str) throws IOException, JsonGenerationException {
        this.delegate.writeFieldName(str);
    }

    public void writeFieldName(SerializedString ss) throws IOException, JsonGenerationException {
        this.delegate.writeFieldName(ss);
    }

    public void writeFieldName(SerializableString ss) throws IOException, JsonGenerationException {
        this.delegate.writeFieldName(ss);
    }

    public void writeString(String str) throws IOException, JsonGenerationException {
        this.delegate.writeString(str);
    }

    public void writeString(char[] chars, int i, int i1) throws IOException, JsonGenerationException {
        this.delegate.writeString(chars, i, i1);
    }

    public void writeString(SerializableString ss) throws IOException, JsonGenerationException {
        this.delegate.writeString(ss);
    }

    public void writeRawUTF8String(byte[] bytes, int i, int i1) throws IOException, JsonGenerationException {
        this.delegate.writeRawUTF8String(bytes, i, i1);
    }

    public void writeUTF8String(byte[] bytes, int i, int i1) throws IOException, JsonGenerationException {
        this.delegate.writeUTF8String(bytes, i, i1);
    }

    public void writeRaw(String str) throws IOException, JsonGenerationException {
        this.delegate.writeRaw(str);
    }

    public void writeRaw(String str, int i, int i1) throws IOException, JsonGenerationException {
        this.delegate.writeRaw(str, i, i1);
    }

    public void writeRaw(char[] chars, int i, int i1) throws IOException, JsonGenerationException {
        this.delegate.writeRaw(chars, i, i1);
    }

    public void writeRaw(char c) throws IOException, JsonGenerationException {
        this.delegate.writeRaw(c);
    }

    public void writeRawValue(String str) throws IOException, JsonGenerationException {
        this.delegate.writeRawValue(str);
    }

    public void writeRawValue(String str, int i, int i1) throws IOException, JsonGenerationException {
        this.delegate.writeRaw(str, i1, i1);
    }

    public void writeRawValue(char[] chars, int i, int i1) throws IOException, JsonGenerationException {
        this.delegate.writeRawValue(chars, i, i1);
    }

    public void writeBinary(Base64Variant bv, byte[] bytes, int i, int i1) throws IOException, JsonGenerationException {
        this.delegate.writeBinary(bv, bytes, i, i1);
    }

    public void writeBinary(byte[] bytes, int i, int i1) throws IOException, JsonGenerationException {
        this.delegate.writeBinary(bytes, i, i1);
    }

    public void writeBinary(byte[] bytes) throws IOException, JsonGenerationException {
        this.delegate.writeBinary(bytes);
    }

    public void writeNumber(int i) throws IOException, JsonGenerationException {
        this.delegate.writeNumber(i);
    }

    public void writeNumber(long l) throws IOException, JsonGenerationException {
        this.delegate.writeNumber(l);
    }

    public void writeNumber(BigInteger bi) throws IOException, JsonGenerationException {
        this.delegate.writeNumber(bi);
    }

    public void writeNumber(double d) throws IOException, JsonGenerationException {
        this.delegate.writeNumber(d);
    }

    public void writeNumber(float f) throws IOException, JsonGenerationException {
        this.delegate.writeNumber(f);
    }

    public void writeNumber(BigDecimal bd) throws IOException, JsonGenerationException {
        this.delegate.writeNumber(bd);
    }

    public void writeNumber(String str) throws IOException, JsonGenerationException, UnsupportedOperationException {
        this.delegate.writeNumber(str);
    }

    public void writeBoolean(boolean bln) throws IOException, JsonGenerationException {
        this.delegate.writeBoolean(bln);
    }

    public void writeNull() throws IOException, JsonGenerationException {
        this.delegate.writeNull();        
    }

    public void writeObject(Object o) throws IOException, JsonProcessingException {
        this.delegate.writeObject(o);        
    }

    public void writeTree(JsonNode jn) throws IOException, JsonProcessingException {
        this.delegate.writeTree(jn);        
    }

    public void writeStringField(String str, String str1) throws IOException, JsonGenerationException {
        this.delegate.writeStringField(str, str1);
    }

    public final void writeBooleanField(String str, boolean bln) throws IOException, JsonGenerationException {
        this.delegate.writeBooleanField(str, bln);
    }

    public final void writeNullField(String str) throws IOException, JsonGenerationException {
        this.delegate.writeNullField(str);
    }

    public final void writeNumberField(String str, int i) throws IOException, JsonGenerationException {
        this.delegate.writeNumberField(str, i);
    }

    public final void writeNumberField(String str, long l) throws IOException, JsonGenerationException {
        this.delegate.writeNumberField(str, l);
    }

    public final void writeNumberField(String str, double d) throws IOException, JsonGenerationException {
        this.delegate.writeNumberField(str, d);
    }

    public final void writeNumberField(String str, float f) throws IOException, JsonGenerationException {
        this.delegate.writeNumberField(str, f);
    }

    public final void writeNumberField(String str, BigDecimal bd) throws IOException, JsonGenerationException {
        this.delegate.writeNumberField(str, bd);
    }

    public final void writeBinaryField(String str, byte[] bytes) throws IOException, JsonGenerationException {
        this.delegate.writeBinaryField(str, bytes);
    }

    public final void writeArrayFieldStart(String str) throws IOException, JsonGenerationException {
        this.delegate.writeArrayFieldStart(str);
    }

    public final void writeObjectFieldStart(String str) throws IOException, JsonGenerationException {
        this.delegate.writeObjectFieldStart(str);
    }

    public final void writeObjectField(String str, Object o) throws IOException, JsonProcessingException {
        // <editor-fold defaultstate="collapsed" desc="Compiled Code">
        /* 0: aload_0
         * 1: aload_1
         * 2: invokevirtual org/codehaus/jackson/JsonGenerator.writeFieldName:(Ljava/lang/String;)V
         * 5: aload_0
         * 6: aload_2
         * 7: invokevirtual org/codehaus/jackson/JsonGenerator.writeObject:(Ljava/lang/Object;)V
         * 10: return
         *  */
        // </editor-fold>
    }

    public void copyCurrentEvent(JsonParser jp) throws IOException, JsonProcessingException {
        this.delegate.copyCurrentEvent(jp);
    }

    public void copyCurrentStructure(JsonParser jp) throws IOException, JsonProcessingException {
        this.delegate.copyCurrentStructure(jp);
    }

    public JsonStreamContext getOutputContext() {
        return this.delegate.getOutputContext();
    }

    public void flush() throws IOException {
        this.delegate.flush();
    }

    public boolean isClosed() {
        return this.delegate.isClosed();
    }

    @Override
    public void close() throws IOException {
        this.delegate.close();
    }
}
