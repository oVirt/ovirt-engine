package org.ovirt.engine.core.utils.serialization.json;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.ovirt.engine.api.extensions.ExtKey;

public class JsonExtKeySerializer extends JsonSerializer<ExtKey> {

    @Override
    public void serialize(ExtKey extKey, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeFieldName(
                String.format("%s;%s;%s",
                        extKey.getUuid().getName(),
                        extKey.getType().getName(),
                        extKey.getUuid().getUuid()));
    }
}
