package org.ovirt.engine.core.common.businessentities.gluster;

import org.ovirt.engine.core.compat.Guid;


public class GlusterVolumeTaskStatusForHost extends GlusterVolumeTaskStatusDetail {
    private static final long serialVersionUID = -1134758927239004415L;

    private String hostName;
    private Guid hostUuid;

    public GlusterVolumeTaskStatusForHost() {
    }

    public String getHostName() {
        return hostName;
    }
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    public Guid getHostUuid() {
        return this.hostUuid;
    }
    public void setHostUuid(Guid id) {
        this.hostUuid = id;
    }
}
