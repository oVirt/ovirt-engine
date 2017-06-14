package org.ovirt.engine.core.utils;


/**
 * Serializer used to serialize a payload in order for it to be saved in DB.
 */
public interface Serializer {
    /**
     * Serialize the given payload.
     *
     * @param payload
     *            the payload to serialize
     * @return object that holds serialization
     */
    public Object serialize(Object payload) throws SerializationException;
}
