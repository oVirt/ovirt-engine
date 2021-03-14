package org.ovirt.engine.core.sso.utils.json;

import java.io.IOException;

import org.ovirt.engine.api.extensions.ExtKey;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JsonExtKeySerializer extends JsonSerializer<ExtKey> {

    @Override
    public void serialize(ExtKey extKey, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeFieldName(
                String.format("%s;%s;%s",
                        extKey.getUuid().getName(),
                        extKey.getType().getName(),
                        extKey.getUuid().getUuid()));
    }
}
