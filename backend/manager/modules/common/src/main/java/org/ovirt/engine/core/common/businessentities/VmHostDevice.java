package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

public class VmHostDevice extends VmDevice {

    /**
     * Spec param flag that distinguishes devices that are intended
     * by user from those that are just placeholders to satisfy the
     * IOMMU group restriction
     */
    public static final String IOMMU_PLACEHOLDER = "iommuPlaceholder";

    public VmHostDevice() {
        setType(VmDeviceGeneralType.HOSTDEV);
        setAddress("");
        setManaged(true);
        setPlugged(true);
        setId(new VmDeviceId());
        setSpecParams(new HashMap<String, Object>());
    }

    public VmHostDevice(VmDevice device) {
        this();
        setId(device.getId());
        setDevice(device.getDevice());
        setSpecParams(device.getSpecParams());
        setAddress(device.getAddress());
        setAlias(device.getAlias());
    }

    public VmHostDevice(Guid vmId, HostDevice device) {
        this();
        setDeviceId(Guid.newGuid());
        setVmId(vmId);
        setDevice(device.getDeviceName());
    }

    public void setIommuPlaceholder(boolean placeholder) {
        getSpecParams().put(IOMMU_PLACEHOLDER, placeholder);
    }

    public boolean isIommuPlaceholder() {
        return isIommuPlaceHolder(getSpecParams());
    }

    public static boolean isIommuPlaceHolder(Map<String, Object> specParams) {
        return Boolean.TRUE.equals(specParams.get(IOMMU_PLACEHOLDER));
    }
}
