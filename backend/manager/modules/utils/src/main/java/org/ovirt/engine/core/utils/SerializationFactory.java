package org.ovirt.engine.core.utils;

import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectSerializer;


public class SerializationFactory {

    private static SerializationFactory factory;
    static {
        factory = new SerializationFactory();
    }

    public static SerializationFactory getFactory() {
        return factory;
    }

    public Serializer createSerializer() {
        return new JsonObjectSerializer();
    }

    public Deserializer createDeserializer() {
        return new JsonObjectDeserializer();
    }
}
