package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public final class HostDeviceId implements Serializable, Comparable<HostDeviceId> {

    private Guid hostId;
    private String deviceName;

    public HostDeviceId() {
    }

    public HostDeviceId(Guid hostId, String deviceName) {
        this.hostId = hostId;
        this.deviceName = deviceName;
    }

    public Guid getHostId() {
        return hostId;
    }

    public void setHostId(Guid hostId) {
        this.hostId = hostId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HostDeviceId other = (HostDeviceId) o;
        return ObjectUtils.objectsEqual(hostId, other.hostId) &&
                ObjectUtils.objectsEqual(deviceName, other.deviceName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hostId == null) ? 0 : hostId.hashCode());
        result = prime * result + ((deviceName == null) ? 0 : deviceName.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("hostId", getHostId())
                .append("deviceName", getDeviceName())
                .build();
    }

    @Override
    public int compareTo(HostDeviceId other) {
        int idComparison = hostId.compareTo(other.hostId);
        if (idComparison == 0) {
            return deviceName.compareTo(other.deviceName);
        } else {
            return idComparison;
        }
    }
}
