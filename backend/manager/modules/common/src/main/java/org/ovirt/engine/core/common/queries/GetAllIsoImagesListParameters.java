package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetAllIsoImagesListParameters extends GetAllImagesListParametersBase {
    private static final long serialVersionUID = 6098440434536241071L;

    public GetAllIsoImagesListParameters() {
    }

    public GetAllIsoImagesListParameters(Guid storageDomainId) {
        sdId = storageDomainId;
    }

    private Guid sdId = new Guid();

    public Guid getStorageDomainId() {
        return sdId;
    }

    public void setStorageDomainId(Guid value) {
        sdId = value;
    }
}
