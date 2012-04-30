package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetImageByImageIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 8163118634744815433L;

    public GetImageByImageIdParameters(Guid imageId) {
        _imageId = imageId;
    }

    private Guid _imageId = new Guid();

    public Guid getImageId() {
        return _imageId;
    }

    public GetImageByImageIdParameters() {
    }
}
