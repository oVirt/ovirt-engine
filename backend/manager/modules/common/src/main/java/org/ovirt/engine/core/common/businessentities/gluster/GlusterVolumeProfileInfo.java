package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

/**
 * Class representing information of a Gluster Volume Profile Info
 *
 */
public class GlusterVolumeProfileInfo implements Serializable {

    private static final long serialVersionUID = -768822766895441186L;
    private Guid volumeId;
    private List<BrickProfileDetails> brickProfileDetails;
    private List<GlusterVolumeProfileStats> nfsProfileDetails;

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

    public List<GlusterVolumeProfileStats> getNfsProfileDetails() {
        return nfsProfileDetails;
    }

    public void setNfsProfileDetails(List<GlusterVolumeProfileStats> nfsProfileDetails) {
        this.nfsProfileDetails = nfsProfileDetails;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if(!(obj instanceof GlusterVolumeProfileInfo)) {
            return false;
        }
        GlusterVolumeProfileInfo other = (GlusterVolumeProfileInfo) obj;
        return Objects.equals(volumeId, other.volumeId)
                && Objects.equals(brickProfileDetails, other.brickProfileDetails)
                && Objects.equals(nfsProfileDetails, other.nfsProfileDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                volumeId,
                brickProfileDetails,
                nfsProfileDetails
        );
    }
}
