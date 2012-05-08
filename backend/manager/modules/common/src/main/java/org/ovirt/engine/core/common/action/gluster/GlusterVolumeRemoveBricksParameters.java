package org.ovirt.engine.core.common.action.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.compat.Guid;

/**
 * Command parameters class with a volume id and brick list as parameters.
 */
public class GlusterVolumeRemoveBricksParameters extends GlusterVolumeParameters {

    private static final long serialVersionUID = -6327827215443668543L;

    private List<GlusterBrickEntity> bricks;

    public GlusterVolumeRemoveBricksParameters(Guid volumeId, List<GlusterBrickEntity> bricks) {
        super(volumeId);
        setBricks(bricks);
    }

    public List<GlusterBrickEntity> getBricks() {
        return bricks;
    }

    public void setBricks(List<GlusterBrickEntity> bricks) {
        this.bricks = bricks;
    }

}
