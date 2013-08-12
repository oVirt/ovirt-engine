package org.ovirt.engine.core.utils.serialization.json;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.std.EnumDeserializer;
import org.codehaus.jackson.map.util.EnumResolver;

/**
 * Custom enum deserializer.
 * Uses the EnumDeserializer and in case of exception.
 * ignore it and return null (in order to avoid cases of unknown literal
 * value).
 */
public class JsonEnumDeserializer extends EnumDeserializer {

    public JsonEnumDeserializer() {
        super(null);
    }

    public JsonEnumDeserializer(EnumResolver<?> res) {
        super(res);
    }

    @Override
    public Enum<?> deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        try {
            return super.deserialize(jp, ctxt);
        } catch (Exception ex) {
            return null;
        }
    }
}
