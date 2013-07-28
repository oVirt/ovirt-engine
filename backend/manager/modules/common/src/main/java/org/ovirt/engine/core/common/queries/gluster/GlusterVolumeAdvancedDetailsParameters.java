package org.ovirt.engine.core.common.queries.gluster;

import org.ovirt.engine.core.compat.Guid;

/**
 * Parameter class with volume name, brick name and details required as parameters. <br>
 * This will be used by Get Gluster Volume Advanced Details Query. <br>
 */
public class GlusterVolumeAdvancedDetailsParameters extends GlusterParameters {
    private static final long serialVersionUID = -1224829720081853632L;

    private Guid volumeId;
    private Guid brickId;
    private boolean detailRequired;

    public GlusterVolumeAdvancedDetailsParameters() {
    }

    public GlusterVolumeAdvancedDetailsParameters(Guid clusterId,
            Guid volumeId,
            Guid brickId,
            boolean detailRequired) {
        super(clusterId);
        setVolumeId(volumeId);
        setBrickId(brickId);
        setDetailRequired(detailRequired);
    }

    public Guid getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
    }

    public Guid getBrickId() {
        return brickId;
    }

    public void setBrickId(Guid brickId) {
        this.brickId = brickId;
    }

    public boolean isDetailRequired() {
        return detailRequired;
    }

    public void setDetailRequired(boolean detailRequired) {
        this.detailRequired = detailRequired;
    }
}
