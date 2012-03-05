package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;


public class ImagesContainterParametersBase extends ImagesActionsParametersBase {
    private static final long serialVersionUID = -5293411452987894523L;
    private static final String DEFAULT_DRIVE = "1";
    private String drive;
    private boolean wipeAfterDelete;
    private Guid containerid = Guid.Empty;

    public ImagesContainterParametersBase() {
    }

    public ImagesContainterParametersBase(Guid imageId) {
        super(imageId);
    }

    public ImagesContainterParametersBase(Guid imageId, String drive, Guid containerId) {
        this(imageId);
        this.drive = StringHelper.isNullOrEmpty(drive) ? DEFAULT_DRIVE : drive;
        containerid = containerId;
    }

    public String getDrive() {
        return drive;
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
