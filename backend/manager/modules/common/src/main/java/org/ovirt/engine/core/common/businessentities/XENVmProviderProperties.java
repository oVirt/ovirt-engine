package org.ovirt.engine.core.common.businessentities;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.Provider.AdditionalProperties;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class XENVmProviderProperties implements AdditionalProperties {

    @NotNull(message = "VALIDATION_URL_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String url;

    private Guid storagePoolId;
    private Guid proxyHostId;

    public XENVmProviderProperties() {
    }

    public XENVmProviderProperties(String url, Guid dataCenterId, Guid proxyHostId) {
        this.url = url;
        this.storagePoolId = dataCenterId;
        this.proxyHostId = proxyHostId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
        return ToStringBuilder.forInstance(this)
                .append("url", getUrl())
                .append("storagePoolId", getStoragePoolId())
                .append("proxyHostId", getProxyHostId())
                .build();
    }
}
