package org.ovirt.engine.core.utils.serialization.json;

import java.util.Set;

import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@SuppressWarnings("serial")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public abstract class JsonVdsDynamicMixIn extends VdsDynamic {
    @JsonIgnore
    @Override
    public abstract Set<StorageFormatType> getSupportedDomainVersions();
}
