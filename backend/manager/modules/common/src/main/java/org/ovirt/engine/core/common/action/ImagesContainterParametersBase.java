package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ImagesContainterParametersBase extends ImagesActionsParametersBase {
    private static final long serialVersionUID = -5293411452987894523L;
    private boolean wipeAfterDelete;
    private Guid containerid;

    public ImagesContainterParametersBase() {
        containerid = Guid.Empty;
    }

    public ImagesContainterParametersBase(Guid imageId) {
        super(imageId);
        containerid = Guid.Empty;
    }

    public ImagesContainterParametersBase(Guid imageId, Guid containerId) {
        this(imageId);
        containerid = containerId;
    }

    public Guid getContainerId() {
        return containerid;
    }

    public boolean getWipeAfterDelete() {
        return wipeAfterDelete;
    }

    public void setWipeAfterDelete(boolean value) {
        wipeAfterDelete = value;
    }
}
