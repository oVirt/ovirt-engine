package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.Set;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class VolumeGeneralModel extends EntityModel<GlusterVolumeEntity> {
    private String name;
    private String volumeId;
    private String volumeType;
    private String replicaCount;
    private String stripeCount;
    private String disperseCount;
    private String redundancyCount;
    private String numOfBricks;
    private String glusterMountPoint;
    private String nfsMountPoint;
    private Set<TransportType> transportTypes;
    private String snapMaxLimit;
    private Long volumeTotalCapacity;
    private Long volumeFreeCapacity;
    private Long volumeUsedCapacity;
    private Long volumeConfirmedFreeCapacity;
    private Long volumeVdoSavings;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    public String getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(String volumeType) {
        this.volumeType = volumeType;
        onPropertyChanged(new PropertyChangedEventArgs("VolumeType")); //$NON-NLS-1$
    }

    public void setVolumeTypeSilently(String volumeType) {
        this.volumeType = volumeType;
    }

    public String getReplicaCount() {
        return replicaCount;
    }

    public void setReplicaCount(String replicaCount) {
        this.replicaCount = replicaCount;
    }

    public String getStripeCount() {
        return stripeCount;
    }

    public void setStripeCount(String stripeCount) {
        this.stripeCount = stripeCount;
    }

    public String getDisperseCount() {
        return disperseCount;
    }

    public void setDisperseCount(String disperseCount) {
        this.disperseCount = disperseCount;
    }

    public String getRedundancyCount() {
        return redundancyCount;
    }

    public void setRedundancyCount(String redundancyCount) {
        this.redundancyCount = redundancyCount;
    }

    public String getNumOfBricks() {
        return numOfBricks;
    }

    public void setNumOfBricks(String numOfBricks) {
        this.numOfBricks = numOfBricks;
    }

    public String getGlusterMountPoint() {
        return glusterMountPoint;
    }

    public void setGlusterMountPoint(String glusterMountPoint) {
        this.glusterMountPoint = glusterMountPoint;
    }

    public String getNfsMountPoint() {
        return nfsMountPoint;
    }

    public void setNfsMountPoint(String nfsMountPoint) {
        this.nfsMountPoint = nfsMountPoint;
    }

    public String getSnapMaxLimit() {
        return this.snapMaxLimit;
    }

    public void setSnapMaxLimit(String noOfSnaps) {
        this.snapMaxLimit = noOfSnaps;
    }

    public VolumeGeneralModel() {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHelpTag(HelpTag.general);
        setHashName("general"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        updatePropeties();
    }

    private void updatePropeties() {
        if (getEntity() == null) {
            return;
        }
        GlusterVolumeEntity entity = getEntity();
        setName(entity.getName());
        setVolumeId(entity.getId() != null ? entity.getId().toString() : null);
        String volumeType =
                entity.getVolumeType() != null ? EnumTranslator.getInstance().translate(entity.getVolumeType()) : null;
        if (entity.getIsArbiter()) {
            volumeType += " " + ConstantsManager.getInstance().getConstants().arbiter(); //$NON-NLS-1$
        }
        setVolumeType(volumeType);
        setReplicaCount(entity.getReplicaCount() != null ? entity.getIsArbiter()
                ? entity.getReplicaCount() - 1 + " + " + 1 : Integer.toString(entity.getReplicaCount()) : null); //$NON-NLS-1$
        setStripeCount(entity.getStripeCount() != null ? Integer.toString(entity.getStripeCount()) : null);
        setDisperseCount(entity.getDisperseCount() != null ? Integer.toString(entity.getDisperseCount()) : null);
        setRedundancyCount(entity.getRedundancyCount() != null ? Integer.toString(entity.getRedundancyCount()) : null);
        setNumOfBricks(entity.getBricks() != null ? Integer.toString(entity.getBricks().size()) : null);
        setTransportTypes(entity.getTransportTypes());
        setSnapMaxLimit(entity.getSnapMaxLimit() != null ? entity.getSnapMaxLimit().toString() : null);
        if(entity.getAdvancedDetails() != null && entity.getAdvancedDetails().getCapacityInfo() != null) {
            setVolumeFreeCapacity(entity.getAdvancedDetails().getCapacityInfo().getFreeSize());
            setVolumeTotalCapacity(entity.getAdvancedDetails().getCapacityInfo().getTotalSize());
            setVolumeUsedCapacity(entity.getAdvancedDetails().getCapacityInfo().getUsedSize());
            setVolumeConfirmedFreeCapacity(entity.getAdvancedDetails().getCapacityInfo().getConfirmedFreeSize());
            setVolumeVdoSavings(entity.getAdvancedDetails().getCapacityInfo().getVdoSavings().longValue());
        } else {
            setVolumeFreeCapacity(null);
            setVolumeTotalCapacity(null);
            setVolumeUsedCapacity(null);
            setVolumeConfirmedFreeCapacity(null);
            setVolumeVdoSavings(null);
        }
    }

    public Set<TransportType> getTransportTypes() {
        return transportTypes;
    }

    public void setTransportTypes(Set<TransportType> transportTypes) {
        this.transportTypes = transportTypes;
    }

    public Long getVolumeTotalCapacity() {
        return volumeTotalCapacity;
    }

    public void setVolumeTotalCapacity(Long volumeTotalCapacity) {
        this.volumeTotalCapacity = volumeTotalCapacity;
    }

    public Long getVolumeFreeCapacity() {
        return volumeFreeCapacity;
    }

    public void setVolumeFreeCapacity(Long volumeFreeCapacity) {
        this.volumeFreeCapacity = volumeFreeCapacity;
    }

    public Long getVolumeUsedCapacity() {
        return volumeUsedCapacity;
    }

    public void setVolumeUsedCapacity(Long volumeUsedCapacity) {
        this.volumeUsedCapacity = volumeUsedCapacity;
    }

    public Long getVolumeConfirmedFreeCapacity() {
        return volumeConfirmedFreeCapacity;
    }

    public void setVolumeConfirmedFreeCapacity(Long volumeConfirmedFreeCapacity) {
        this.volumeConfirmedFreeCapacity = volumeConfirmedFreeCapacity;
    }

    public Long getVolumeVdoSavings() {
        return volumeVdoSavings;
    }

    public void setVolumeVdoSavings(Long volumeVdoSavings) {
        this.volumeVdoSavings = volumeVdoSavings;
    }
}
