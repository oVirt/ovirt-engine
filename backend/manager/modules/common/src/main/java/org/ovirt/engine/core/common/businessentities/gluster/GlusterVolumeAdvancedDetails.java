package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.List;

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

    public GlusterVolumeAdvancedDetails() {
    }

    public void copyDetailsFrom(GlusterVolumeAdvancedDetails volumeAdvancedDetails) {
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
     *
     * @param from
     * @param to
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
}
