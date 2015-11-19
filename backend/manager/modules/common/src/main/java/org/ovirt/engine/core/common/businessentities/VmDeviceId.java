package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class VmDeviceId implements Serializable, Comparable<VmDeviceId> {

    private static final long serialVersionUID = 7807607542617897504L;
    private Guid deviceId;
    private Guid vmId;

    public VmDeviceId() {
    }

    public VmDeviceId(Guid deviceId, Guid vmId) {
        this.deviceId = deviceId;
        this.vmId = vmId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                deviceId,
                vmId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VmDeviceId)) {
            return false;
        }
        VmDeviceId other = (VmDeviceId) obj;
        return Objects.equals(deviceId, other.deviceId)
                && Objects.equals(vmId, other.vmId);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("deviceId", getDeviceId())
                .append("vmId", getVmId())
                .build();
    }

    public Guid getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Guid deviceId) {
        this.deviceId = deviceId;
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    @Override
    public int compareTo(VmDeviceId other) {
        int vmComparsion = getVmId().compareTo(other.getVmId());
        if (vmComparsion == 0) {
            return getDeviceId().compareTo(other.getDeviceId());
        } else {
            return vmComparsion;
        }
    }
}
