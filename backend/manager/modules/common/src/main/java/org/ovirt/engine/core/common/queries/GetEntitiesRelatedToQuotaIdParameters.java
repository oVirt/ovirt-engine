package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetEntitiesRelatedToQuotaIdParameters extends VdcQueryParametersBase {

    /**
     * Generate serial version Id.
     */
    private static final long serialVersionUID = 6230356218250899258L;

    public GetEntitiesRelatedToQuotaIdParameters() {
    }

    public Guid getQuotaId() {
        return quotaId;
    }

    public void setQuotaId(Guid quotaId) {
        this.quotaId = quotaId;
    }

    private Guid quotaId;
}
