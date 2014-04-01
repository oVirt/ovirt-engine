package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class RemoveDiskSnapshotsParameters extends ImagesContainterParametersBase implements Serializable {

    private static final long serialVersionUID = -629355522841577585L;

    private ArrayList<Guid> imageIds;

    public RemoveDiskSnapshotsParameters(Guid imageId) {
        this(new ArrayList<Guid>(Arrays.asList(imageId)));
    }

    public RemoveDiskSnapshotsParameters(ArrayList<Guid> imageIds) {
        this.imageIds = imageIds;
    }

    public RemoveDiskSnapshotsParameters() {
    }

    public ArrayList<Guid> getImageIds() {
        return imageIds;
    }

    public void setImageIds(ArrayList<Guid> imageIds) {
        this.imageIds = imageIds;
    }
}
