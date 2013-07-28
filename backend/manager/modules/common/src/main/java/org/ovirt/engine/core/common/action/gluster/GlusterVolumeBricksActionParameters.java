package org.ovirt.engine.core.common.action.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeBricksActionParameters extends GlusterVolumeBricksParameters {

    private static final long serialVersionUID = -2254020786236387303L;
    private int replicaCount = 0;
    private int stripeCount = 0;

    public GlusterVolumeBricksActionParameters() {
    }

    public GlusterVolumeBricksActionParameters(Guid volumeId, List<GlusterBrickEntity> bricks, int replicaCount, int stripeCount) {
        super(volumeId, bricks);
        setReplicaCount(replicaCount);
        setStripeCount(stripeCount);
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


}
