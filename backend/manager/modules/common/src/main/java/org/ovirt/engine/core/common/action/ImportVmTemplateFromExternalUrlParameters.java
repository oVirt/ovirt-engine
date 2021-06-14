package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.ovirt.engine.core.compat.Guid;

public class ImportVmTemplateFromExternalUrlParameters extends ActionParametersBase {

    @NotEmpty
    private String url;
    private String newTemplateName;
    private Guid proxyHostId;

    @NotNull
    private Guid storageDomainId;

    private Guid quotaId;
    private Guid cpuProfileId;

    @NotNull
    private Guid clusterId;
    private boolean importAsNewEntity;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNewTemplateName() {
        return newTemplateName;
    }

    public void setNewTemplateName(String newTemplateName) {
        this.newTemplateName = newTemplateName;
    }

    public Guid getProxyHostId() {
        return proxyHostId;
    }

    public void setProxyHostId(Guid proxyHostId) {
        this.proxyHostId = proxyHostId;
    }

    public Guid getQuotaId() {
        return quotaId;
    }

    public void setQuotaId(Guid quotaId) {
        this.quotaId = quotaId;
    }

    public Guid getCpuProfileId() {
        return cpuProfileId;
    }

    public void setCpuProfileId(Guid cpuProfileId) {
        this.cpuProfileId = cpuProfileId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public boolean isImportAsNewEntity() {
        return importAsNewEntity;
    }

    public void setImportAsNewEntity(boolean importAsNewEntity) {
        this.importAsNewEntity = importAsNewEntity;
    }
}
