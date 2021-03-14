package org.ovirt.engine.core.utils.serialization.json;

import java.util.Map;

import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@SuppressWarnings("serial")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public abstract class JsonAddVmTemplateParametersMixIn extends AddVmTemplateParameters {

    @JsonIgnore
    @Override
    public abstract Map<Guid, DiskImage> getDiskInfoDestinationMap();

    @JsonIgnore
    @Override
    public abstract VM getVm();

}
