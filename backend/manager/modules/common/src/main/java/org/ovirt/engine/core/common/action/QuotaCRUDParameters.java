package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.compat.Guid;

public class QuotaCRUDParameters extends StoragePoolParametersBase implements Serializable {
    private static final long serialVersionUID = -3821623510049174551L;

    private Guid quotaId;
    private Quota quota;

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
}
