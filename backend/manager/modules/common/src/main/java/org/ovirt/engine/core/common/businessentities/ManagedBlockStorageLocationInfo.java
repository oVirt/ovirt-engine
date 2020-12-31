package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.compat.Guid;

public class ManagedBlockStorageLocationInfo extends LocationInfo {
    private String url;
    private Map<String, Object> lease;
    private Integer generation;
    private boolean zeroed;
    private VolumeFormat format;
    private Guid storageDomainId;

    public ManagedBlockStorageLocationInfo(String url,
            Map<String, Object> lease,
            Integer generation,
            VolumeFormat format,
            boolean zeroed,
            Guid storageDomainId) {
        this.url = url;
        this.lease = lease;
        this.generation = generation;
        this.format = format;
        this.zeroed = zeroed;
        this.storageDomainId = storageDomainId;
    }

    public ManagedBlockStorageLocationInfo() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Object> getLease() {
        return lease;
    }

    public void setLease(Map<String, Object> lease) {
        this.lease = lease;
    }

    public Integer getGeneration() {
        return generation;
    }

    public void setGeneration(Integer generation) {
        this.generation = generation;
    }

    public VolumeFormat getFormat() {
        return format;
    }

    public void setFormat(VolumeFormat format) {
        this.format = format;
    }

    public boolean isZeroed() {
        return zeroed;
    }

    public void setZeroed(boolean zeroed) {
        this.zeroed = zeroed;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ManagedBlockStorageLocationInfo)) {
            return false;
        }
        ManagedBlockStorageLocationInfo that = (ManagedBlockStorageLocationInfo) o;
        return zeroed == that.zeroed &&
                Objects.equals(url, that.url) &&
                Objects.equals(lease, that.lease) &&
                Objects.equals(generation, that.generation) &&
                format == that.format &&
                Objects.equals(storageDomainId, that.storageDomainId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, lease, generation, zeroed, format, storageDomainId);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ManagedBlockStorageLocationInfo.class.getSimpleName() + "[", "]")
                .add("url='" + url + "'")
                .add("lease=" + lease)
                .add("generation=" + generation)
                .add("zeroed=" + zeroed)
                .add("format=" + format)
                .add("storageDomainId=" + storageDomainId)
                .toString();
    }
}
