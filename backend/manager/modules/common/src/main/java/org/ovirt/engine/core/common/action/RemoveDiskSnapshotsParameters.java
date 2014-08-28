package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RemoveDiskSnapshotsParameters extends ImagesContainterParametersBase implements Serializable {

    private static final long serialVersionUID = -629355522841577585L;

    private ArrayList<Guid> imageIds;

    // The following is used to persist data during command execution
    private boolean imageIdsSorted;
    private boolean isLiveMerge;
    private List<Guid> childImageIds;

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

    public boolean isImageIdsSorted() {
        return imageIdsSorted;
    }

    public void setImageIdsSorted(boolean imageIdsSorted) {
        this.imageIdsSorted = imageIdsSorted;
    }

    public boolean isLiveMerge() {
        return isLiveMerge;
    }

    public void setLiveMerge(boolean isLiveMerge) {
        this.isLiveMerge = isLiveMerge;
    }

    public List<Guid> getChildImageIds() {
        return childImageIds;
    }

    public void setChildImageIds(List<Guid> childImageIds) {
        this.childImageIds = childImageIds;
    }
}
