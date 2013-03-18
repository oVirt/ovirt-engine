package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetStorageDomainsByImageIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 8163118634744815433L;

    public GetStorageDomainsByImageIdParameters(Guid imageId) {
        _imageId = imageId;
    }

    private Guid _imageId = new Guid();

    public Guid getImageId() {
        return _imageId;
    }

    public GetStorageDomainsByImageIdParameters() {
    }
}
