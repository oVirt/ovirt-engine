package org.ovirt.engine.core.utils.serialization.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

@SuppressWarnings("serial")
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY)
public abstract class JsonVmStaticMixIn extends VmStatic {

    @JsonIgnore
    @Override
    public abstract Map<Guid, VmDevice> getManagedDeviceMap();

    @JsonIgnore
    @Override
    public abstract List<VmDevice> getUnmanagedDeviceList();

    @JsonIgnore
    @Override
    public abstract ArrayList<DiskImage> getImages();

}
