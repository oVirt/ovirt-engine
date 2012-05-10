package org.ovirt.engine.core.common.vdscommands.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeBricksActionVDSParameters extends GlusterVolumeBricksVDSParameters {
    private int replicaCount;
    private int stripeCount;

    public GlusterVolumeBricksActionVDSParameters(Guid serverId,
            String volumeName,
            List<GlusterBrickEntity> bricks,
            int replicaCount,
            int stripeCount) {
        super(serverId, volumeName, bricks);
        this.replicaCount = replicaCount;
        this.stripeCount = stripeCount;
    }


    public int getReplicaCount() {
        return replicaCount;
    }

    public int getStripeCount() {
        return stripeCount;
    }

}
