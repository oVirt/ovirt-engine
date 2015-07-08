package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ExtendImageSizeParameters extends ImagesActionsParametersBase {

    private long newSize;
    private boolean parentNotifiesCallback;

    public ExtendImageSizeParameters() {
        super();
    }

    public ExtendImageSizeParameters(Guid imageId, long newSize) {
        super(imageId);
        this.newSize = newSize;
    }

    public ExtendImageSizeParameters(Guid imageId, long newSize, boolean parentNotifiesCallback) {
        this(imageId, newSize);
        this.parentNotifiesCallback = parentNotifiesCallback;
    }

    public long getNewSize() {
        return newSize;
    }

    public void setNewSize(long newSize) {
        this.newSize = newSize;
    }

    public long getNewSizeInGB() {
        return this.newSize / (1024 * 1024 * 1024);
    }

    public boolean getParentNotifiesCallback() {
        return parentNotifiesCallback;
    }
}
