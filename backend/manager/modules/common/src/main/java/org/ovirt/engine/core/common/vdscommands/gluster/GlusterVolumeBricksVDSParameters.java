package org.ovirt.engine.core.common.vdscommands.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeBricksVDSParameters extends GlusterVolumeVDSParameters {
    private List<GlusterBrickEntity> bricks;

    public GlusterVolumeBricksVDSParameters(Guid serverId,
            String volumeName,
            List<GlusterBrickEntity> bricks) {
        super(serverId, volumeName);
        this.bricks = bricks;
    }

    public GlusterVolumeBricksVDSParameters() {
    }

    public List<GlusterBrickEntity> getBricks() {
        return bricks;
    }
}
