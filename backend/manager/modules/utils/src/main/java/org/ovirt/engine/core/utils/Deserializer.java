package org.ovirt.engine.core.utils;

import java.io.Serializable;

/**
 * Deserializer of payload from DB.
 */
public interface Deserializer {

    /**
     * Deserializes the payload given to it back to an object of type T.
     * @param source source
     * @param type The type or the return value which is expected.
     * @return The deserialized payload
     */
    public <T extends Serializable> T deserialize(Object source, Class<T> type) throws SerializationException;

}
