package org.ovirt.engine.core.common.businessentities;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class VmwareVmProviderProperties extends VmProviderProperties {

    @NotNull(message = "VALIDATION_URL_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String vCenter;

    @NotNull(message = "VALIDATION_URL_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String esx;

    @NotNull(message = "VALIDATION_URL_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String dataCenter;

    private boolean verifySSL;

    public VmwareVmProviderProperties() {
    }

    public VmwareVmProviderProperties(String vCenter, String esx, String dataCenter,
            boolean verifySSL, Guid dataCenterId, Guid proxyHostId) {
        super(dataCenterId, proxyHostId);
        this.setvCenter(vCenter);
        this.esx = esx;
        this.dataCenter = dataCenter;
        this.verifySSL = verifySSL;
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

    @Override
    protected ToStringBuilder getToStringBuilder() {
        return super.getToStringBuilder()
                .append("vCenter", getvCenter())
                .append("ESX", getEsx())
                .append("dataCenter", getDataCenter())
                .append("verifySSL", isVerifySSL());
    }
}
