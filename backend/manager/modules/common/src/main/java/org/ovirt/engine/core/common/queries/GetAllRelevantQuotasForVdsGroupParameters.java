package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetAllRelevantQuotasForVdsGroupParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = 8048618567614936452L;
    private Guid vdsGroupId;

    public GetAllRelevantQuotasForVdsGroupParameters() {
        this(new Guid());
    }

    public GetAllRelevantQuotasForVdsGroupParameters(Guid storageId) {
        this.vdsGroupId = storageId;
    }

    public Guid getVdsGroupId() {
        return vdsGroupId;
    }
}
