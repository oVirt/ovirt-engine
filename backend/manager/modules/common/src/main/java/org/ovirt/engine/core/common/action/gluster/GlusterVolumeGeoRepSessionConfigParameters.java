package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeGeoRepSessionConfigParameters extends GlusterVolumeGeoRepSessionParameters {

    private static final long serialVersionUID = 1L;

    private String configKey;
    private String configValue;

    public GlusterVolumeGeoRepSessionConfigParameters() {
        super();
    }

    public GlusterVolumeGeoRepSessionConfigParameters(Guid volumeId, Guid sessionId, String configKey, String configValue) {
        super(volumeId, sessionId);
        this.configKey = configKey;
        this.configValue = configValue;
    }

    public GlusterVolumeGeoRepSessionConfigParameters(Guid volumeId, Guid sessionId, String configKey) {
        super(volumeId, sessionId);
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
