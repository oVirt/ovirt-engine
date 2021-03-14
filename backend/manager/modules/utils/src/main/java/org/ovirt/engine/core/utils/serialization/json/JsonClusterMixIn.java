package org.ovirt.engine.core.utils.serialization.json;

import java.util.Set;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@SuppressWarnings("serial")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public abstract class JsonClusterMixIn extends Cluster {

    @JsonIgnore
    @Override
    public abstract  Set<VmRngDevice.Source> getRequiredRngSources();
}
