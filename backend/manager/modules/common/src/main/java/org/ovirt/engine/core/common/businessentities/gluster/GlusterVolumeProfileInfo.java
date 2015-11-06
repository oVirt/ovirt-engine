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
        if(!(obj instanceof GlusterVolumeProfileInfo)) {
            return false;
        }
        GlusterVolumeProfileInfo profileInfo = (GlusterVolumeProfileInfo) obj;
        if(!Objects.equals(getVolumeId(), profileInfo.getVolumeId())){
            return false;
        }
        if(!Objects.equals(getBrickProfileDetails(), profileInfo.getBrickProfileDetails())) {
            return false;
        }
        if(!Objects.equals(getNfsProfileDetails(), profileInfo.getNfsProfileDetails())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getVolumeId() == null) ? 0 : getVolumeId().hashCode());
        result = prime * result + ((getBrickProfileDetails() == null) ? 0 : getBrickProfileDetails().hashCode());
        result = prime * result + ((getNfsProfileDetails() == null) ? 0 : getNfsProfileDetails().hashCode());
        return result;
    }
}
