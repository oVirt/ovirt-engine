package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetQuotaVdsGroupByQuotaIdQueryParameters extends VdcQueryParametersBase {

    /**
     * Generated serial Id.
     */
    private static final long serialVersionUID = -4984247560936769620L;
    private Guid quotaId;

    public void setQuotaId(Guid quotaId) {
        this.quotaId = quotaId;
    }

    public Guid getQuotaId() {
        return quotaId;
    }
}
