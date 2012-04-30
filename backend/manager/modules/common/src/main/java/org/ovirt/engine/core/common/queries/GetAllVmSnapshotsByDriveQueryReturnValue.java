package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetAllVmSnapshotsByDriveQueryReturnValue extends VdcQueryReturnValue {
    private static final long serialVersionUID = 3743404728664179142L;
    private Guid _tryingImage = new Guid();

    /**
     * Gets or sets the trying image.
     *
     * <value>The trying image.</value>
     */
    public Guid getTryingImage() {
        return _tryingImage;
    }

    public void setTryingImage(Guid value) {
        _tryingImage = value;
    }

    public GetAllVmSnapshotsByDriveQueryReturnValue() {
    }
}
