package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class VolumeGeneralModel extends EntityModel {
    private String name;
    private String volumeType;
    private String replicaCount;
    private String numOfBricks;
    private String glusterMountPoint;
    private String nfsMountPoint;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(String volumeType) {
        this.volumeType = volumeType;
    }

    public String getReplicaCount() {
        return replicaCount;
    }

    public void setReplicaCount(String replicaCount) {
        this.replicaCount = replicaCount;
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

    @Override
    protected void OnEntityChanged() {
        super.OnEntityChanged();
        updatePropeties();
    }

    private void updatePropeties() {
        if (getEntity() == null) {
            return;
        }
        GlusterVolumeEntity entity = (GlusterVolumeEntity) getEntity();
        setName(entity.getName());
        setVolumeType(entity.getVolumeType().toString());
        setReplicaCount(Integer.toString(entity.getReplicaCount()));
        setNumOfBricks(Integer.toString(entity.getBricks().size()));
    }

}
