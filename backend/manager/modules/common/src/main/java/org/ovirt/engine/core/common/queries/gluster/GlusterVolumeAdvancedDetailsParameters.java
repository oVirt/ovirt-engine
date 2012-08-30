package org.ovirt.engine.core.common.queries.gluster;

import org.ovirt.engine.core.compat.Guid;

/**
 * Parameter class with volume name, brick name and details required as parameters. <br>
 * This will be used by Get Gluster Volume Advanced Details Query. <br>
 */
public class GlusterVolumeAdvancedDetailsParameters extends GlusterParameters {
    private static final long serialVersionUID = -1224829720081853632L;

    private String volumeName;
    private String brickName;
    private boolean detailRequired;

    public GlusterVolumeAdvancedDetailsParameters(Guid clusterId,
            String volumeName,
            String brickName,
            boolean detailRequired) {
        super(clusterId);
        setVolumeName(volumeName);
        setBrickName(brickName);
        setDetailRequired(detailRequired);
    }

    public String getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }

    public String getBrickName() {
        return brickName;
    }

    public void setBrickName(String brickName) {
        this.brickName = brickName;
    }

    public boolean isDetailRequired() {
        return detailRequired;
    }

    public void setDetailRequired(boolean detailRequired) {
        this.detailRequired = detailRequired;
    }
}
