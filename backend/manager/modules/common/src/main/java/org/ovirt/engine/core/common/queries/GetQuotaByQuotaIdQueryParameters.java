package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetQuotaByQuotaIdQueryParameters extends VdcQueryParametersBase {

    /**
     * Generated serial Id.
     */
    private static final long serialVersionUID = 6780385841336933228L;
    private Guid quotaId;

    public void setQuotaId(Guid quotaId) {
        this.quotaId = quotaId;
    }

    public Guid getQuotaId() {
        return quotaId;
    }
}
