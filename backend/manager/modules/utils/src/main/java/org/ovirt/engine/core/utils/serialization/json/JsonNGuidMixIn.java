package org.ovirt.engine.core.utils.serialization.json;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

/**
 * This mix-in annotations class is used for annotating the {@link NGuid} class not to recursively try to
 * {@link NGuid#getValue()}, otherwise Jackson can't serialize & deserialize it.
 */
@SuppressWarnings("serial")
@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_ARRAY)
public abstract class JsonNGuidMixIn extends NGuid {

    /**
     * Tells Jackson that the constructor with the {@link String} argument is to be used to deserialize the entity,
     * using the "uuid" property as the argument.
     *
     * @param candidate
     */
    @JsonCreator
    public JsonNGuidMixIn(@JsonProperty("uuid") String candidate) {
        super(candidate);
    }

    /**
     * Ignore this method since Jackson will try to recursively dereference it and fail to serialize.
     */
    @JsonIgnore
    @Override
    public abstract Guid getValue();
}
