package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class VmTemplateParametersBase extends VdcActionParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -8930994274659598061L;
    private boolean removeTemplateFromDb;
    private Guid vmTemplateId = Guid.Empty;
    private Guid quotaId;
    private boolean privateCheckDisksExists;

    public boolean getCheckDisksExists() {
        return privateCheckDisksExists;
    }

    public void setCheckDisksExists(boolean value) {
        privateCheckDisksExists = value;
    }

    public VmTemplateParametersBase(Guid vmTemplateId) {
        this.vmTemplateId = vmTemplateId;
    }

    public Guid getVmTemplateId() {
        return vmTemplateId;
    }

    private List<Guid> privateStorageDomainsList;

    public List<Guid> getStorageDomainsList() {
        return privateStorageDomainsList;
    }

    public void setStorageDomainsList(List<Guid> value) {
        privateStorageDomainsList = value;
    }

    public VmTemplateParametersBase() {
    }

    public void setRemoveTemplateFromDb(boolean removeTemplateFromDb) {
        this.removeTemplateFromDb = removeTemplateFromDb;
    }

    public boolean isRemoveTemplateFromDb() {
        return removeTemplateFromDb;
    }

    public Guid getQuotaId() {
        return quotaId;
    }

    public void setQuotaId(Guid value) {
        quotaId = value;
    }
}
