package org.ovirt.engine.core.utils;

import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectSerializer;


public class SerializationFactory {

    private static final JsonObjectSerializer serializer = new JsonObjectSerializer();
    private static final JsonObjectDeserializer deserializer = new JsonObjectDeserializer();

    public static JsonObjectSerializer getSerializer() {
        return serializer;
    }

    public static JsonObjectDeserializer getDeserializer() {
        return deserializer;
    }
}
