package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetDiskImageByDiskAndImageIdsParameters extends QueryParametersBase {
    private static final long serialVersionUID = 5425102428441282208L;

    private Guid diskId;
    private Guid imageId;

    public GetDiskImageByDiskAndImageIdsParameters() {
    }

    public GetDiskImageByDiskAndImageIdsParameters(Guid diskId, Guid imageId) {
        this.diskId = diskId;
        this.imageId = imageId;
    }

    public Guid getDiskId() {
        return diskId;
    }

    public Guid getImageId() {
        return imageId;
    }
}
