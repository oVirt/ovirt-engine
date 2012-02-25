package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetDisksByQuotaIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1277692201283671492L;

    public GetDisksByQuotaIdParameters(Guid quotaId) {
        this.quotaId = quotaId;
    }

    private Guid quotaId = new Guid();

    public Guid getQuotaId() {
        return quotaId;
    }

    public GetDisksByQuotaIdParameters() {
    }
}
