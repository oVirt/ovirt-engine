package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetQuotaStorageByQuotaIdQueryParameters extends VdcQueryParametersBase {

    /**
     * Generated serial Id.
     */
    private static final long serialVersionUID = 7391876800293075864L;

    private Guid quotaId;

    public void setQuotaId(Guid quotaId) {
        this.quotaId = quotaId;
    }

    public Guid getQuotaId() {
        return quotaId;
    }
}
