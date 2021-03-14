package org.ovirt.engine.core.utils.serialization.json;

import org.ovirt.engine.core.compat.Guid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This mix-in annotations class is used for annotating the {@link Guid} so the
 * non-default constructor can be used for deserializing it.
 */
@SuppressWarnings("serial")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_ARRAY)
public abstract class JsonGuidMixIn extends Guid {

    /**
     * Tells Jackson that the constructor with the {@link String} argument is to be used to deserialize the entity,
     * using the "uuid" property as the argument.
     */
    @JsonCreator
    public JsonGuidMixIn(@JsonProperty("uuid") String candidate) {
        super(candidate);
    }
}
