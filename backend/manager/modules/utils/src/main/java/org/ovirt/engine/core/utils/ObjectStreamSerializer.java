package org.ovirt.engine.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Uses ObjectOutputStream as serializer, the returned object is array of bytes
 */
public class ObjectStreamSerializer implements Serializer {

    /**
     *
     */
    public ObjectStreamSerializer() {
    }

    @Override
    public Object serialize(Serializable payload) throws SerializationExeption {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
            objectOutputStream.writeObject(payload);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new SerializationExeption(e);
        }

    }

}
