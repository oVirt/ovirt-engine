package org.ovirt.engine.core.common.businessentities;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class XENVmProviderProperties extends VmProviderProperties {

    @NotNull(message = "VALIDATION_URL_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String url;

    public XENVmProviderProperties() {
    }

    public XENVmProviderProperties(String url, Guid dataCenterId, Guid proxyHostId) {
        super(dataCenterId, proxyHostId);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    protected ToStringBuilder getToStringBuilder() {
        return super.getToStringBuilder()
                .append("url", getUrl());
    }
}
