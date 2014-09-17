package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeGeoRepSessionVDSParameters extends GlusterVolumeVDSParameters {

    private String slaveHost;
    private String slaveVolume;
    private Boolean force = false;

    public GlusterVolumeGeoRepSessionVDSParameters() {

    }

    public GlusterVolumeGeoRepSessionVDSParameters(Guid serverId, String volumeName) {
        super(serverId, volumeName);
    }

    public GlusterVolumeGeoRepSessionVDSParameters(Guid serverId, String volumeName, String slaveHost, String slaveVolume) {
        super(serverId, volumeName);
        this.slaveHost = slaveHost;
        this.slaveVolume = slaveVolume;
    }

    public GlusterVolumeGeoRepSessionVDSParameters(Guid serverId, String volumeName, String slaveHost, String slaveVolume,
            Boolean force) {
        super(serverId, volumeName);
        this.slaveHost = slaveHost;
        this.slaveVolume = slaveVolume;
        this.force = force;
    }

    public String getSlaveHost() {
        return slaveHost;
    }

    public void setSlaveHost(String slaveHost) {
        this.slaveHost = slaveHost;
    }

    public String getSlaveVolume() {
        return slaveVolume;
    }

    public void setSlaveVolume(String slaveVolume) {
        this.slaveVolume = slaveVolume;
    }

    public Boolean getForce() {
        return force;
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

}
