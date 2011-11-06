package org.ovirt.engine.core.utils.serialization.json;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.queries.ValueObjectPair;

@SuppressWarnings("serial")
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY)
public abstract class JsonValueObjectMapMixIn extends ValueObjectMap {
    /**
     * Ignore this method since Jackson will try to recursively dereference it and fail to serialize.
     */
    @JsonIgnore
    @Override
    public abstract ArrayList<ValueObjectPair> getValuePairs();
}
