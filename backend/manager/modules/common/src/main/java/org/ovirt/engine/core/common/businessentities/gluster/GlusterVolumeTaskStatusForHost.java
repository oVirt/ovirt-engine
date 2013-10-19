package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.Comparator;

import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeTaskStatusForHost extends GlusterVolumeTaskStatusDetail implements Comparator<GlusterVolumeTaskStatusForHost>, Serializable {
    private static final long serialVersionUID = -1134758927239004415L;

    private String hostName;
    private Guid hostUuid;
    private Guid hostId;

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

    public Guid getHostId() {
        return hostId;
    }

    public void setHostId(Guid hostId) {
        this.hostId = hostId;
    }

    @Override
    public int compare(GlusterVolumeTaskStatusForHost arg0, GlusterVolumeTaskStatusForHost arg1) {
        return arg0.getHostName().compareTo(arg1.getHostName());
    }

}
