package org.ovirt.engine.core.common.businessentities.gluster;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.gluster.AddBrick;
import org.ovirt.engine.core.common.validation.group.gluster.RemoveBrick;
import org.ovirt.engine.core.compat.Guid;

/**
 * Brick is the building block of a Gluster Volume. It represents a directory on one of the servers of the cluster, and
 * is typically represented in the form <b>serverName:brickDirectory</b><br>
 * It also has a status (ONLINE / OFFLINE) which represents the status of the brick process that runs on the server to
 * which the brick belongs.
 *
 * @see GlusterVolumeEntity
 * @see GlusterBrickStatus
 */
public class GlusterBrickEntity extends IVdcQueryable implements BusinessEntityWithStatus<Guid, GlusterStatus>, GlusterTaskSupport, Nameable {
    private static final long serialVersionUID = 7119439284741452278L;

    @NotNull(message = "VALIDATION.GLUSTER.BRICK.ID.NOT_NULL", groups = { RemoveBrick.class })
    private Guid id;

    @NotNull(message = "VALIDATION.GLUSTER.VOLUME.ID.NOT_NULL", groups = { AddBrick.class })
    private Guid volumeId;

    private String volumeName;

    @NotNull(message = "VALIDATION.GLUSTER.VOLUME.BRICK.SERVER_ID.NOT_NULL", groups = { CreateEntity.class })
    private Guid serverId;

    private Guid networkId;

    private String networkAddress;

    private String serverName;

    @NotNull(message = "VALIDATION.GLUSTER.VOLUME.BRICK.BRICK_DIR.NOT_NULL", groups = { CreateEntity.class })
    private String brickDirectory;

    private GlusterStatus status;

    private Integer brickOrder;

    private BrickDetails brickDetails;

    private GlusterAsyncTask asyncTask;

    public GlusterBrickEntity() {
        status = GlusterStatus.DOWN;
        asyncTask = new GlusterAsyncTask();
    }

    public Guid getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
    }

    public Guid getServerId() {
        return serverId;
    }

    public void setServerId(Guid serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setBrickDirectory(String brickDirectory) {
        this.brickDirectory = brickDirectory;
    }

    public String getBrickDirectory() {
        return brickDirectory;
    }

    public Guid getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Guid networkId) {
        this.networkId = networkId;
    }

    public String getNetworkAddress() {
        return networkAddress;
    }

    public void setNetworkAddress(String networkAddress) {
        this.networkAddress = networkAddress;
    }

    @Override
    public GlusterStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(GlusterStatus status) {
        this.status = status;
    }

    public boolean isOnline() {
        return status == GlusterStatus.UP;
    }

    public String getQualifiedName() {
        if (networkId != null && networkAddress != null && !networkAddress.isEmpty()) {
            return networkAddress + ":" + brickDirectory;
        }
        return serverName + ":" + brickDirectory;
    }

    @Override
    public String toString() {
        return getQualifiedName() + "(" + serverName + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getId().hashCode();
        result = prime * result + ((volumeId == null) ? 0 : volumeId.hashCode());
        result = prime * result + ((serverId == null) ? 0 : serverId.hashCode());
        result = prime * result + ((brickDirectory == null) ? 0 : brickDirectory.hashCode());
        result = prime * result + ((brickOrder == null) ? 0 : brickOrder.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((asyncTask == null) ? 0 : asyncTask.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GlusterBrickEntity)) {
            return false;
        }

        GlusterBrickEntity brick = (GlusterBrickEntity) obj;
        return (getId().equals(brick.getId())
                && (ObjectUtils.objectsEqual(volumeId, brick.getVolumeId()))
                && (ObjectUtils.objectsEqual(serverId, brick.getServerId()))
                && (ObjectUtils.objectsEqual(brickDirectory, brick.getBrickDirectory()))
                && (ObjectUtils.objectsEqual(brickOrder, brick.getBrickOrder()))
                && (ObjectUtils.objectsEqual(getAsyncTask(), brick.getAsyncTask()))
                && status == brick.getStatus());
    }

    public void copyFrom(GlusterBrickEntity brick) {
        setId(brick.getId());
        setVolumeId(brick.getVolumeId());
        setServerId(brick.getServerId());
        setServerName(brick.getServerName());
        setBrickDirectory(brick.getBrickDirectory());
        setBrickOrder(brick.getBrickOrder());
        setStatus(brick.getStatus());
    }

    /**
     * Generates the id if not present. Volume brick doesn't have an id in
     * GlusterFS, and hence is generated on the backend side.
     * @return id of the brick
     */
    @Override
    public Guid getId() {
        return getId(true);
    }

    public Guid getId(boolean generateIfNull) {
        if(id == null && generateIfNull) {
            id = Guid.newGuid();
        }
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public Integer getBrickOrder() {
        return brickOrder;
    }

    public void setBrickOrder(Integer brickOrder) {
        this.brickOrder = brickOrder;
    }

    public BrickDetails getBrickDetails() {
        return brickDetails;
    }

    public void setBrickDetails(BrickDetails brickDetails) {
        this.brickDetails = brickDetails;
    }

    public BrickProperties getBrickProperties() {
        if (brickDetails != null) {
            return brickDetails.getBrickProperties();
        }
        return null;
    }
    @Override
    public GlusterAsyncTask getAsyncTask() {
       return asyncTask;
    }

    @Override
    public void setAsyncTask(GlusterAsyncTask asyncTask) {
        this.asyncTask = asyncTask;
    }

    public String getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }

    @Override
    public String getName() {
        return getQualifiedName();
    }

}
