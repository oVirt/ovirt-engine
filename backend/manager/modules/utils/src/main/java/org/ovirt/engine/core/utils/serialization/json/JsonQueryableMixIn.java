package org.ovirt.engine.core.utils.serialization.json;

import org.ovirt.engine.core.common.businessentities.Queryable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@SuppressWarnings("serial")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public abstract class JsonQueryableMixIn implements Queryable {

    @JsonIgnore
    public abstract Object getQueryableId();
}
