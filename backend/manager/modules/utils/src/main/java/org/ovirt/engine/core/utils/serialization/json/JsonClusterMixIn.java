package org.ovirt.engine.core.utils.serialization.json;

import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;

@SuppressWarnings("serial")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public abstract class JsonClusterMixIn extends Cluster {

    @JsonIgnore
    @Override
    public abstract  Set<VmRngDevice.Source> getRequiredRngSources();
}
