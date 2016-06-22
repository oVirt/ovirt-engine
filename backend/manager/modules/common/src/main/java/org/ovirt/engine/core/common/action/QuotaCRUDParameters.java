package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.compat.Guid;

public class QuotaCRUDParameters extends StoragePoolParametersBase implements Serializable {
    private static final long serialVersionUID = -3821623510049174551L;

    @Valid
    private Guid quotaId;

    @Valid
    @NotNull
    private Quota quota;

    private boolean copyPermissions;

    public QuotaCRUDParameters() {
    }

    public QuotaCRUDParameters(Quota quota) {
        quotaId = quota.getId();
        this.quota = quota;
    }

    public Guid getQuotaId() {
        return quotaId;
    }

    public void setQuotaId(Guid value) {
        this.quotaId = value;
    }

    public Quota getQuota() {
        return quota;
    }

    public void setQuota(Quota value) {
        quota = value;
    }

    public boolean isCopyPermissions() {
        return copyPermissions;
    }

    public void setCopyPermissions(boolean copyPermissions) {
        this.copyPermissions = copyPermissions;
    }
}
