package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class ReduceSANStorageDomainDevicesCommandParameters extends StorageDomainParametersBase {
    private int removeIndex = 0;
    private List<String> devicesToReduce;
    private List<String> dstDevices;

    public ReduceSANStorageDomainDevicesCommandParameters() {
    }

    public ReduceSANStorageDomainDevicesCommandParameters(Guid storageDomainId, List<String> devicesToReduce) {
        super(storageDomainId);
        this.devicesToReduce = devicesToReduce;
    }

    public List<String> getDevicesToReduce() {
        return devicesToReduce;
    }

    public void setDevicesToReduce(List<String> devicesToReduce) {
        this.devicesToReduce = devicesToReduce;
    }

    public int getRemoveIndex() {
        return removeIndex;
    }

    public void setRemoveIndex(int removeIndex) {
        this.removeIndex = removeIndex;
    }

    public List<String> getDstDevices() {
        return dstDevices;
    }

    public void setDstDevices(List<String> dstDevices) {
        this.dstDevices = dstDevices;
    }
}
