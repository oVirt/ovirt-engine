package org.ovirt.engine.core.utils.serialization.json;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.ovirt.engine.core.common.businessentities.VdsFencingOptions;
import org.ovirt.engine.core.common.queries.ValueObjectMap;

@SuppressWarnings("serial")
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY)
public abstract class JsonVdsFencingOptionsMixIn extends VdsFencingOptions {

    @JsonIgnore
    @Override
    public abstract ValueObjectMap getFencingAgentInstanceOptionsMap();

    @JsonIgnore
    @Override
    public abstract ValueObjectMap getFencingOptionMappingMap();

    @JsonIgnore
    @Override
    public abstract ValueObjectMap getFencingOptionTypesMap();
}
