package org.ovirt.engine.core.common.businessentities;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.Provider.AdditionalProperties;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class VmwareVmProviderProperties implements AdditionalProperties {

    @NotNull(message = "VALIDATION_URL_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String vCenter;

    @NotNull(message = "VALIDATION_URL_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String esx;

    @NotNull(message = "VALIDATION_URL_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String dataCenter;

    private boolean verifySSL;
    private Guid storagePoolId;
    private Guid proxyHostId;

    public VmwareVmProviderProperties() {
    }

    public VmwareVmProviderProperties(String vCenter, String esx, String dataCenter,
            boolean verifySSL, Guid dataCenterId, Guid proxyHostId) {
        this.setvCenter(vCenter);
        this.esx = esx;
        this.dataCenter = dataCenter;
        this.verifySSL = verifySSL;
        this.storagePoolId = dataCenterId;
        this.proxyHostId = proxyHostId;
    }

    public String getvCenter() {
        return vCenter;
    }

    public void setvCenter(String vCenter) {
        this.vCenter = vCenter;
    }

    public String getEsx() {
        return esx;
    }

    public void setEsx(String esx) {
        this.esx = esx;
    }

    public String getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(String dataCenter) {
        this.dataCenter = dataCenter;
    }

    public boolean isVerifySSL() {
        return verifySSL;
    }

    public void setVerifySSL(boolean verifySSL) {
        this.verifySSL = verifySSL;
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
                .append("vCenter", getvCenter())
                .append("ESX", getEsx())
                .append("dataCenter", getDataCenter())
                .append("verifySSL", isVerifySSL())
                .append("storagePoolId", getStoragePoolId())
                .append("proxyHostId", getProxyHostId())
                .build();
    }
}
