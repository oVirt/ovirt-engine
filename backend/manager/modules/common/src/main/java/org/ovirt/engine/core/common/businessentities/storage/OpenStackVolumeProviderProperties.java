package org.ovirt.engine.core.common.businessentities.storage;

import org.ovirt.engine.core.common.businessentities.TenantProviderProperties;
import org.ovirt.engine.core.compat.Guid;

public class OpenStackVolumeProviderProperties extends TenantProviderProperties {

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        OpenStackVolumeProviderProperties that = (OpenStackVolumeProviderProperties) o;

        if (storagePoolId != null ? !storagePoolId.equals(that.storagePoolId) : that.storagePoolId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (storagePoolId != null ? storagePoolId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OpenStackVolumeProviderProperties [pluginType=")
                .append(", tenantName=")
                .append(getTenantName())
                .append("]");
        return builder.toString();
    }

}
