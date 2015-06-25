package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeGeoRepSessionParameters extends GlusterVolumeParameters{

    private static final long serialVersionUID = -881348048838907389L;

    private Guid geoRepSessionId;
    private String slaveVolumeName;
    private Guid slaveHostId;
    private String userName;
    private String userGroup;
    private boolean force;

    public GlusterVolumeGeoRepSessionParameters() {
        super();
    }

    public GlusterVolumeGeoRepSessionParameters(Guid volumeId, Guid geoRepSessionId) {
        super(volumeId);
        this.geoRepSessionId = geoRepSessionId;
    }

    public GlusterVolumeGeoRepSessionParameters(Guid volumeId, String slaveVolumeName, Guid slaveHostId) {
        this(volumeId, slaveVolumeName, slaveHostId, "root", null, false);
    }

    public GlusterVolumeGeoRepSessionParameters(Guid volumeId, Guid geoRepSessionId, boolean force) {
        super(volumeId);
        this.geoRepSessionId = geoRepSessionId;
        this.force = force;
    }

    public GlusterVolumeGeoRepSessionParameters(Guid volumeId,
            String slaveVolumeName,
            Guid slaveHostId,
            String userName,
            String userGroup,
            boolean force) {
        super(volumeId);
        this.slaveVolumeName = slaveVolumeName;
        this.slaveHostId = slaveHostId;
        this.userName = userName;
        this.userGroup = userGroup;
        this.force = force;
    }

    public GlusterVolumeGeoRepSessionParameters(Guid volumeId, String slaveVolumeName,
            Guid slaveHostId,
            String userName,
            String userGroup) {
        this(volumeId, slaveVolumeName, slaveHostId, userName, userGroup, false);
    }

    public String getSlaveVolumeName() {
        return slaveVolumeName;
    }

    public void setSlaveVolumeName(String slaveVolumeName) {
        this.slaveVolumeName = slaveVolumeName;
    }

    public Guid getSlaveHostId() {
        return slaveHostId;
    }

    public void setSlaveHostId(Guid slaveHostId) {
        this.slaveHostId = slaveHostId;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public Guid getGeoRepSessionId() {
        return geoRepSessionId;
    }

    public void setGeoRepSessionId(Guid geoRepSessionId) {
        this.geoRepSessionId = geoRepSessionId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }
}
