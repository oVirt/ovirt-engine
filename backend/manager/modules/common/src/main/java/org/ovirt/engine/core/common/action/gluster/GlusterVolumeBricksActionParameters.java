package org.ovirt.engine.core.common.action.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeBricksActionParameters extends GlusterVolumeBricksParameters {

    private static final long serialVersionUID = -2254020786236387303L;
    private int replicaCount;
    private int stripeCount;
    private boolean force;

    public GlusterVolumeBricksActionParameters() {
    }

    public GlusterVolumeBricksActionParameters(Guid volumeId, List<GlusterBrickEntity> bricks, int replicaCount, int stripeCount) {
        this(volumeId, bricks, replicaCount, stripeCount, false);
    }

    public GlusterVolumeBricksActionParameters(Guid volumeId,
            List<GlusterBrickEntity> bricks,
            int replicaCount,
            int stripeCount,
            boolean force) {
        super(volumeId, bricks);
        setReplicaCount(replicaCount);
        setStripeCount(stripeCount);
        setForce(force);
    }

    public int getReplicaCount() {
        return replicaCount;
    }

    public void setReplicaCount(int replicaCount) {
        this.replicaCount = replicaCount;
    }

    public int getStripeCount() {
        return stripeCount;
    }

    public void setStripeCount(int stripeCount) {
        this.stripeCount = stripeCount;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}
