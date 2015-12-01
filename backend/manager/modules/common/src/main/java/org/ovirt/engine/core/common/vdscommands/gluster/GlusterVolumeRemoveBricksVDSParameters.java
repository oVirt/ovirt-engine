package org.ovirt.engine.core.common.vdscommands.gluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.compat.Guid;

/**
 * VDS parameters class with Server Id, volume name and brick list as parameters,
 * apart from volume name inherited from {@link GlusterVolumeVDSParameters}.
 * Used by the Gluster Volume Remove Brick command.
 */
public class GlusterVolumeRemoveBricksVDSParameters extends GlusterVolumeVDSParameters {

    private List<GlusterBrickEntity> bricks;

    private int replicaCount;
    private boolean forceRemove;

    public GlusterVolumeRemoveBricksVDSParameters(Guid serverId, String volumeName, List<GlusterBrickEntity> bricks) {
        super(serverId, volumeName);
        this.bricks = bricks;
    }

    public GlusterVolumeRemoveBricksVDSParameters(Guid serverId,
            String volumeName,
            List<GlusterBrickEntity> bricks,
            int replicaCount) {
        this(serverId, volumeName, bricks, replicaCount, false);
    }

    public GlusterVolumeRemoveBricksVDSParameters(Guid serverId,
            String volumeName,
            List<GlusterBrickEntity> bricks,
            int replicaCount, boolean forceRemove) {
        super(serverId, volumeName);
        this.bricks = bricks;
        this.replicaCount = replicaCount;
        this.forceRemove = forceRemove;
    }

    public GlusterVolumeRemoveBricksVDSParameters() {
    }

    public List<GlusterBrickEntity> getBricks() {
        return bricks;
    }

    public void setBricks(List<GlusterBrickEntity> bricks) {
        this.bricks = bricks;
    }

    public List<String> getBrickDirectories() {
        List<String> brickDirectories = new ArrayList<>();
        for (GlusterBrickEntity brick : getBricks()) {
            brickDirectories.add(brick.getQualifiedName());
        }
        return brickDirectories;
    }

    public int getReplicaCount() {
        return replicaCount;
    }

    public boolean isForceRemove() {
        return forceRemove;
    }

    public void setForceRemove(boolean forceRemove) {
        this.forceRemove = forceRemove;
    }
}
