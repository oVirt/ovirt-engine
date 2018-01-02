package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeResetBrickActionParameters extends GlusterVolumeActionParameters {

    private static final long serialVersionUID = -772657702887532278L;

    private GlusterBrickEntity existingBrick;

    public GlusterVolumeResetBrickActionParameters() {
    }

    public GlusterVolumeResetBrickActionParameters(Guid volumeId,
            GlusterBrickEntity existingBrick) {
        super(volumeId, true);
        setExistingBrick(existingBrick);
    }

    public GlusterBrickEntity getExistingBrick() {
        return existingBrick;
    }

    public void setExistingBrick(GlusterBrickEntity existingBrick) {
        this.existingBrick = existingBrick;
    }
}
