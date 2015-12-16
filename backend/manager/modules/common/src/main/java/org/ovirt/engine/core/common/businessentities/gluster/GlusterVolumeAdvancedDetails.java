package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

/**
 * The gluster volume status info. GlusterFS provides lot of status information about the volume brick. This will
 * consolidate all the status information
 *
 * @see BrickProperties
 * @see GlusterClientInfo
 * @see MemoryStatus
 */
public class GlusterVolumeAdvancedDetails implements Serializable {

    private static final long serialVersionUID = -1134758927239004412L;

    private Guid volumeId;

    private List<BrickDetails> brickDetails;
    private List<GlusterServerService> serviceInfo;
    private GlusterVolumeSizeInfo capacityInfo;
    private Date updatedAt;

    public GlusterVolumeAdvancedDetails() {
    }

    public void copyDetailsFrom(GlusterVolumeAdvancedDetails volumeAdvancedDetails) {
        setCapacityInfo(volumeAdvancedDetails.getCapacityInfo());
        for (BrickDetails newBrickDetails : volumeAdvancedDetails.getBrickDetails()) {
            Guid newBrickId = newBrickDetails.getBrickProperties().getBrickId();
            if (newBrickId != null) {
                for (BrickDetails brickDetails : getBrickDetails()) {
                    if (newBrickId.equals(brickDetails.getBrickProperties().getBrickId())) {
                        copyBrickProperties(newBrickDetails.getBrickProperties(), brickDetails.getBrickProperties());
                        break;
                    }
                }
            }
        }
    }

    /**
     * Note: pid, status and port are not copied as they are already populated in the 'from' object
     */
    private void copyBrickProperties(BrickProperties from, BrickProperties to) {
        to.setTotalSize(from.getTotalSize());
        to.setFreeSize(from.getFreeSize());
        to.setDevice(from.getDevice());
        to.setBlockSize(from.getBlockSize());
        to.setMntOptions(from.getMntOptions());
        to.setFsName(from.getFsName());
    }

    public void copyClientsFrom(GlusterVolumeAdvancedDetails volumeAdvancedDetails) {
        for (BrickDetails newBrickDetails : volumeAdvancedDetails.getBrickDetails()) {
            Guid newBrickId = newBrickDetails.getBrickProperties().getBrickId();
            if (newBrickId != null) {
                for (BrickDetails brickDetails : getBrickDetails()) {
                    if (newBrickId.equals(brickDetails.getBrickProperties().getBrickId())) {
                        brickDetails.setClients(newBrickDetails.getClients());
                        break;
                    }
                }
            }
        }
    }

    public void copyMemoryFrom(GlusterVolumeAdvancedDetails volumeAdvancedDetails) {
        for (BrickDetails newBrickDetails : volumeAdvancedDetails.getBrickDetails()) {
            Guid newBrickId = newBrickDetails.getBrickProperties().getBrickId();
            if (newBrickId != null) {
                for (BrickDetails brickDetails : getBrickDetails()) {
                    if (newBrickId.equals(brickDetails.getBrickProperties().getBrickId())) {
                        brickDetails.setMemoryStatus(newBrickDetails.getMemoryStatus());
                        break;
                    }
                }
            }
        }
    }

    public Guid getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
    }

    public List<BrickDetails> getBrickDetails() {
        return brickDetails;
    }

    public void setBrickDetails(List<BrickDetails> brickDetails) {
        this.brickDetails = brickDetails;
    }

    public List<GlusterServerService> getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(List<GlusterServerService> serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public GlusterVolumeSizeInfo getCapacityInfo() {
        return capacityInfo;
    }

    public void setCapacityInfo(GlusterVolumeSizeInfo capacityInfo) {
        this.capacityInfo = capacityInfo;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                capacityInfo,
                serviceInfo,
                brickDetails,
                volumeId,
                updatedAt
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterVolumeAdvancedDetails)) {
            return false;
        }
        GlusterVolumeAdvancedDetails other = (GlusterVolumeAdvancedDetails) obj;
        return Objects.equals(capacityInfo, other.capacityInfo)
                && Objects.equals(brickDetails, other.brickDetails)
                && Objects.equals(serviceInfo, other.serviceInfo)
                && Objects.equals(updatedAt, other.updatedAt)
                && Objects.equals(volumeId, other.volumeId);
    }
}
