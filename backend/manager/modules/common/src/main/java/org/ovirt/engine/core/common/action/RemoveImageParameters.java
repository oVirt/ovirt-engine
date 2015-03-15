package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageDbOperationScope;
import org.ovirt.engine.core.compat.Guid;

public class RemoveImageParameters extends ImagesContainterParametersBase implements Serializable {
    private static final long serialVersionUID = -225670698019388215L;

    private DiskImage diskImage;
    private ImageDbOperationScope dbOperationScope;
    private boolean removeFromSnapshots;
    private boolean shouldLockImage;

    public RemoveImageParameters(Guid imageId) {
        super(imageId, null);
        setForceDelete(false);
        dbOperationScope = ImageDbOperationScope.IMAGE;
        shouldLockImage= true;
    }

    public RemoveImageParameters() {
    }

    public DiskImage getDiskImage() {
        return diskImage;
    }

    public void setDiskImage(DiskImage value) {
        diskImage = value;
    }

    public ImageDbOperationScope getDbOperationScope() {
        return dbOperationScope;
    }

    public boolean isShouldLockImage() {
        return shouldLockImage;
    }

    public void setShouldLockImage(boolean shouldLockImage) {
        this.shouldLockImage = shouldLockImage;
    }

    public void setDbOperationScope(ImageDbOperationScope dbOperationScope) {
        this.dbOperationScope = dbOperationScope;
    }

    public boolean isRemoveFromSnapshots() {
        return removeFromSnapshots;
    }

    public void setRemoveFromSnapshots(boolean removeFromSnapshots) {
        this.removeFromSnapshots = removeFromSnapshots;
    }
}
