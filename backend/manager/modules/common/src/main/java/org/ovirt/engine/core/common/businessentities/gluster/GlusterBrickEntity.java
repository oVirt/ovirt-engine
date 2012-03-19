package org.ovirt.engine.core.common.businessentities.gluster;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
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
public class GlusterBrickEntity extends IVdcQueryable {
    private static final long serialVersionUID = 7119439284741452278L;

    private Guid volumeId;
    private Guid serverId;
    private String serverName;
    private String brickDirectory;
    private GlusterBrickStatus status = GlusterBrickStatus.DOWN;

    public GlusterBrickEntity() {
    }

    public GlusterBrickEntity(Guid volumeId, VdsStatic server, String brickDirectory, GlusterBrickStatus brickStatus) {
        setVolumeId(volumeId);
        setServerId(server.getId());
        setServerName(server.gethost_name());
        setBrickDirectory(brickDirectory);
        setStatus(brickStatus);
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

    public GlusterBrickStatus getStatus() {
        return status;
    }

    public void setStatus(GlusterBrickStatus status) {
        this.status = status;
    }

    public boolean isOnline() {
        return status == GlusterBrickStatus.UP;
    }

    public String getQualifiedName() {
        return serverName + ":" + brickDirectory;
    }

    @Override
    public String toString() {
        return getQualifiedName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((volumeId == null) ? 0 : volumeId.hashCode());
        result = prime * result + ((serverId == null) ? 0 : serverId.hashCode());
        result = prime * result + ((serverName == null) ? 0 : serverName.hashCode());
        result = prime * result + ((brickDirectory == null) ? 0 : brickDirectory.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GlusterBrickEntity)) {
            return false;
        }

        GlusterBrickEntity brick = (GlusterBrickEntity) obj;
        return (volumeId.equals(brick.getVolumeId())
                && serverId.equals(brick.getServerId())
                && serverName.equals(brick.getServerName())
                && brickDirectory.equals(brick.getBrickDirectory())
                && status == brick.getStatus());
    }

    public void copyFrom(GlusterBrickEntity brick) {
        setVolumeId(brick.getVolumeId());
        setServerId(brick.getServerId());
        setServerName(brick.getServerName());
        setBrickDirectory(brick.getBrickDirectory());
        setStatus(brick.getStatus());
    }
}
