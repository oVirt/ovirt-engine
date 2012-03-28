package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetAllRelevantQuotasForStorageParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = -6391356895440138819L;
    private Guid storageId;

    public GetAllRelevantQuotasForStorageParameters() {
        this(new Guid());
    }

    public GetAllRelevantQuotasForStorageParameters(Guid storageId) {
        this.storageId = storageId;
    }

    public Guid getStorageId() {
        return storageId;
    }
}
