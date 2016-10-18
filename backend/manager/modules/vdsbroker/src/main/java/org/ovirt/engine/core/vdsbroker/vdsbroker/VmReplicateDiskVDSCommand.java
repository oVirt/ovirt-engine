package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.VmReplicateDiskParameters;
import org.springframework.util.StringUtils;

public abstract class VmReplicateDiskVDSCommand<P extends VmReplicateDiskParameters> extends VdsBrokerCommand<P> {

    public VmReplicateDiskVDSCommand(P parameters) {
        super(parameters);
    }

    protected Map<String, Object> getDstDisk() {
        Map<String, Object> dstDisk = new HashMap<>();
        dstDisk.put(VdsProperties.Device, "disk");
        dstDisk.put(VdsProperties.PoolId, getParameters().getStoragePoolId().toString());
        dstDisk.put(VdsProperties.DomainId, getParameters().getTargetStorageDomainId().toString());
        dstDisk.put(VdsProperties.ImageId, getParameters().getImageGroupId().toString());
        dstDisk.put(VdsProperties.VolumeId, getParameters().getImageId().toString());
        if (!StringUtils.isEmpty(getParameters().getDiskType())) {
            dstDisk.put(VdsProperties.DiskType, getParameters().getDiskType());
        }
        return dstDisk;
    }

    protected Map<String, Object> getSrcDisk() {
        Map<String, Object> srcDisk = new HashMap<>();
        srcDisk.put(VdsProperties.Device, "disk");
        srcDisk.put(VdsProperties.PoolId, getParameters().getStoragePoolId().toString());
        srcDisk.put(VdsProperties.DomainId, getParameters().getSrcStorageDomainId().toString());
        srcDisk.put(VdsProperties.ImageId, getParameters().getImageGroupId().toString());
        srcDisk.put(VdsProperties.VolumeId, getParameters().getImageId().toString());
        return srcDisk;
    }
}

