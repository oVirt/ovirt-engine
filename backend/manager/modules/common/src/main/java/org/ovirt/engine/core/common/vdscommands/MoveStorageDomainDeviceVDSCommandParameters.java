package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class MoveStorageDomainDeviceVDSCommandParameters extends StorageJobVdsCommandParameters {
    private String srcDeviceId;
    private List<String> dstDevicesIds;

    public MoveStorageDomainDeviceVDSCommandParameters() {
    }

    public MoveStorageDomainDeviceVDSCommandParameters(Guid jobId, Guid storageDomainId, String srcDeviceId, List<String> dstDevicesIds) {
        super(storageDomainId, jobId);
        this.srcDeviceId = srcDeviceId;
        this.dstDevicesIds = dstDevicesIds;
    }

    public String getSrcDeviceId() {
        return srcDeviceId;
    }

    public void setSrcDeviceId(String srcDeviceId) {
        this.srcDeviceId = srcDeviceId;
    }

    public List<String> getDstDevicesIds() {
        return dstDevicesIds;
    }

    public void setDstDevicesIds(List<String> dstDevicesIds) {
        this.dstDevicesIds = dstDevicesIds;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb).append("srcDeviceId", srcDeviceId).append("dstDevicesIds", dstDevicesIds);
    }
}
