package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeGeoRepConfigVdsParameters extends GlusterVolumeGeoRepSessionVDSParameters {

    private String configKey;
    private String configValue;

    public GlusterVolumeGeoRepConfigVdsParameters() {
        super();
    }

    public GlusterVolumeGeoRepConfigVdsParameters(Guid serverId, String volumeName, String slaveHost, String slaveVolume, String configKey, String configValue) {
        super(serverId, volumeName, slaveHost, slaveVolume);
        this.configKey = configKey;
        this.configValue = configValue;
    }

    public GlusterVolumeGeoRepConfigVdsParameters(Guid serverId,
            String volumeName,
            String slaveHost,
            String slaveVolume,
            String configKey,
            String configValue,
            String userName) {
        super(serverId, volumeName, slaveHost, slaveVolume, userName);
        this.configKey = configKey;
        this.configValue = configValue;
    }

    public GlusterVolumeGeoRepConfigVdsParameters(Guid serverId, String volumeName, String slaveHost, String slaveVolume, String configKey) {
        super(serverId, volumeName, slaveHost, slaveVolume);
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }
}
