package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class RemoveImageParameters extends ImagesContainterParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -7905125604587768041L;

    private DiskImage diskImage;
    private boolean removeFromDB;
    private boolean removeDuringExecution = true;

    public RemoveImageParameters(Guid imageId) {
        super(imageId, null);
        setForceDelete(false);
        removeFromDB = false;
    }

    public RemoveImageParameters() {
    }

    public DiskImage getDiskImage() {
        return diskImage;
    }

    public void setDiskImage(DiskImage value) {
        diskImage = value;
    }

    public boolean isRemoveDuringExecution() {
        return removeDuringExecution;
    }

    public void setRemoveDuringExecution(boolean removeDuringExecution) {
        this.removeDuringExecution = removeDuringExecution;
    }

    public void setRemoveFromDB(boolean removeFromDB) {
        this.removeFromDB = removeFromDB;
    }

    public boolean getRemoveFromDB() {
        return removeFromDB;
    }
}
