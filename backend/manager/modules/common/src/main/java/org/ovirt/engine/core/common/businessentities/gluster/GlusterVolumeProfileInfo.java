package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.compat.Guid;

/**
 * Class representing information of a Gluster Volume Profile Info
 *
 */
public class GlusterVolumeProfileInfo implements Serializable {

    private static final long serialVersionUID = -768822766895441186L;
    private Guid volumeId;
    private List<BrickProfileDetails> brickProfileDetails;

    public GlusterVolumeProfileInfo() {
    }

    public Guid getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
    }

    public List<BrickProfileDetails> getBrickProfileDetails() {
        return brickProfileDetails;
    }

    public void setBrickProfileDetails(List<BrickProfileDetails> brickProfileDetails) {
        this.brickProfileDetails = brickProfileDetails;
    }

}
