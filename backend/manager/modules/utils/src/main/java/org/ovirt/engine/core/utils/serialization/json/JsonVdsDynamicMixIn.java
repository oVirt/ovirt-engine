package org.ovirt.engine.core.utils.serialization.json;

import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;

@SuppressWarnings("serial")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public abstract class JsonVdsDynamicMixIn extends VdsDynamic {
    @JsonIgnore
    @Override
    public abstract Set<StorageFormatType> getSupportedDomainVersions();
}
