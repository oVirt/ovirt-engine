package org.ovirt.engine.core.common.vdscommands.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class GlusterVolumeBricksActionVDSParameters extends GlusterVolumeBricksVDSParameters {
    private int replicaCount;
    private int stripeCount;
    private Version clusterVersion;
    private boolean force;

    public GlusterVolumeBricksActionVDSParameters(Guid serverId,
            String volumeName,
            List<GlusterBrickEntity> bricks,
            int replicaCount,
            int stripeCount,
            Version clusterVersion,
            boolean force) {
        super(serverId, volumeName, bricks);
        this.replicaCount = replicaCount;
        this.stripeCount = stripeCount;
        this.clusterVersion = clusterVersion;
        this.force = force;
    }

    public GlusterVolumeBricksActionVDSParameters() {
    }

    public int getReplicaCount() {
        return replicaCount;
    }

    public int getStripeCount() {
        return stripeCount;
    }

    public Version getClusterVersion() {
        return clusterVersion;
    }

    public boolean isForce() {
        return force;
    }
}
