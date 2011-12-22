package org.ovirt.engine.core.utils.serialization.json;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.ValueObjectMap;

@SuppressWarnings("serial")
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY)
public abstract class JsonVmMixIn extends VM {

    @JsonIgnore
    @Override
    public abstract ValueObjectMap getDiskValueObjectMap();

}
