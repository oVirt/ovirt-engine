package org.ovirt.engine.core.common.action;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class VmSlaPolicyParameters extends VmOperationParameterBase {

    private static final long serialVersionUID = 3918909396931144459L;

    private CpuQos cpuQos;
    private Map<DiskImage, StorageQos> storageQos = new HashMap<>();

    public VmSlaPolicyParameters(Guid vmId) {
        super(vmId);
    }

    public VmSlaPolicyParameters(Guid vmId, CpuQos cpuQos) {
        super(vmId);
        this.cpuQos = cpuQos;
    }

    public VmSlaPolicyParameters() {
    }

    public CpuQos getCpuQos() {
        return cpuQos;
    }

    public void setCpuQos(CpuQos cpuQos) {
        this.cpuQos = cpuQos;
    }

    public Map<DiskImage, StorageQos> getStorageQos() {
        return storageQos;
    }

    public void setStorageQos(Map<DiskImage, StorageQos> storageQosList) {
        this.storageQos = storageQosList;
    }

    public boolean isEmpty() {
        return cpuQos == null && (storageQos == null || storageQos.isEmpty());
    }
}
