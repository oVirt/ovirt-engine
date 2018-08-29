package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.OpenStackProviderProperties;
import org.ovirt.engine.core.compat.Guid;

public class OpenStackVolumeProviderProperties extends OpenStackProviderProperties {

    private static final long serialVersionUID = -3887979451360188295L;

    private Guid storagePoolId;

    public OpenStackVolumeProviderProperties() {
    }

    public OpenStackVolumeProviderProperties(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OpenStackVolumeProviderProperties)) {
            return false;
        }
        OpenStackVolumeProviderProperties other = (OpenStackVolumeProviderProperties) obj;
        return super.equals(obj)
                && Objects.equals(storagePoolId, other.storagePoolId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                storagePoolId
        );
    }
}
