package org.ovirt.engine.core.common.businessentities.gluster;


public class GlusterVolumeTaskStatusForHost extends GlusterVolumeTaskStatusDetail {
    private static final long serialVersionUID = -1134758927239004415L;

    private String hostName;

    public GlusterVolumeTaskStatusForHost() {
    }

    public String getHostName() {
        return hostName;
    }
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
