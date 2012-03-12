package org.ovirt.engine.core.utils.serialization.json;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.ovirt.engine.core.common.action.AddVmFromSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;

@SuppressWarnings("serial")
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY)
public abstract class JsonAddVmFromSnapshotParametersMixIn extends AddVmFromSnapshotParameters {

    @JsonIgnore
    @Override
    public abstract ArrayList<DiskImage> getDiskInfoList();
}
