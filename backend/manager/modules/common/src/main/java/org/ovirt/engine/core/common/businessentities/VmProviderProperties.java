package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class VmProviderProperties implements Provider.AdditionalProperties {

    private Guid storagePoolId;
    private Guid proxyHostId;

    public VmProviderProperties() {
    }

    public VmProviderProperties(Guid dataCenterId, Guid proxyHostId) {
        this.storagePoolId = dataCenterId;
        this.proxyHostId = proxyHostId;
    }

    public Guid getProxyHostId() {
        return proxyHostId;
    }

    public void setProxyHostId(Guid proxyHostId) {
        this.proxyHostId = proxyHostId;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid dataCenterId) {
        this.storagePoolId = dataCenterId;
    }

    @Override
    public String toString() {
        return getToStringBuilder().build();
    }

    protected ToStringBuilder getToStringBuilder() {
        return ToStringBuilder.forInstance(this)
                .append("storagePoolId", getStoragePoolId())
                .append("proxyHostId", getProxyHostId());
    }
}
