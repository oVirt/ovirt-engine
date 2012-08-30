package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.compat.Guid;

/**
 * VDS parameter class with clusterId, volume name, brick name, details required as parameter. <br>
 * This will be used directly by Get Gluster Volume Advanced Details Query <br>
 */
public class GlusterVolumeAdvancedDetailsVDSParameters extends GlusterVolumeVDSParameters {
    private Guid clusterId;
    private String brickName;
    private boolean detailRequired;

    public GlusterVolumeAdvancedDetailsVDSParameters(Guid upServerId,
            Guid clusterId,
            String volumeName,
            String brickName,
            boolean detailRequired) {
        super(upServerId, volumeName);
        this.clusterId = clusterId;
        this.brickName = brickName;
        this.detailRequired = detailRequired;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public boolean isDetailRequired() {
        return detailRequired;
    }

    public String getBrickName() {
        return brickName;
    }

}
