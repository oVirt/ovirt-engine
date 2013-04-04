package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class RemoveImageParameters extends ImagesContainterParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -7905125604587768041L;

    private DiskImage diskImage;
    private boolean removeFromDB;
    private boolean removeFromSnapshots;

    public RemoveImageParameters(Guid imageId) {
        super(imageId, null);
        setForceDelete(false);
        removeFromDB = true;
    }

    public RemoveImageParameters() {
    }

    public DiskImage getDiskImage() {
        return diskImage;
    }

    public void setDiskImage(DiskImage value) {
        diskImage = value;
    }

    public void setRemoveFromDB(boolean removeFromDB) {
        this.removeFromDB = removeFromDB;
    }

    public boolean getRemoveFromDB() {
        return removeFromDB;
    }

    public boolean isRemoveFromSnapshots() {
        return removeFromSnapshots;
    }

    public void setRemoveFromSnapshots(boolean removeFromSnapshots) {
        this.removeFromSnapshots = removeFromSnapshots;
    }
}
