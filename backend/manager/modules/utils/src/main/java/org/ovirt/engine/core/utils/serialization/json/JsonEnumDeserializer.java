package org.ovirt.engine.core.utils.serialization.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.EnumDeserializer;
import com.fasterxml.jackson.databind.util.EnumResolver;

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

    public JsonEnumDeserializer(EnumResolver res) {
        super(res);
    }

    @Override
    public Enum<?> deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        try {
            return (Enum<?>) super.deserialize(jp, ctxt);
        } catch (Exception ex) {
            return null;
        }
    }
}
