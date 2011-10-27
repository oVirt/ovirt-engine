package org.ovirt.engine.core.utils;

import java.io.Serializable;

/**
 * Serializer used to serialize a payload in order for it to be saved in DB.
 */
public interface Serializer {
    /**
     * Serialize the given payload.
     * @param payload to serialize
     * @return object that holds serialization
     * @throws SnapshotSerializationExeption
     */
    public Object serialize(Serializable payload) throws SerializationExeption;
}
