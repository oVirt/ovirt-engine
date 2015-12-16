package org.ovirt.engine.core.utils.serialization.json;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.ovirt.engine.core.compat.Guid;

/**
 * This mix-in annotations class is used for annotating the {@link Guid} so the
 * non-default constructor can be used for deserializing it.
 */
@SuppressWarnings("serial")
@JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_ARRAY)
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
