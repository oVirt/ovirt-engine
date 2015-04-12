package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.compat.Guid;

public class SetUpMountBrokerParameters extends IdParameters {

    private static final long serialVersionUID = 1L;

    private String remoteVolumeName;
    private String remoteUserName;
    private String remoteUserGroup;
    private boolean partial;

    public SetUpMountBrokerParameters() {
        super();
    }

    public SetUpMountBrokerParameters(Guid remoteHostId, String remoteVolumeName, String remoteUserName, String remoteUserGroup) {
        this(remoteHostId, remoteVolumeName, remoteUserName, remoteUserGroup, false);
    }

    public SetUpMountBrokerParameters(Guid remoteHostId, String remoteVolumeName,
            String remoteUserName,
            String remoteUserGroup,
            boolean partial) {
        super(remoteHostId);
        this.remoteVolumeName = remoteVolumeName;
        this.remoteUserName = remoteUserName;
        this.remoteUserGroup = remoteUserGroup;
        this.partial = partial;
    }

    public String getRemoteUserName() {
        return remoteUserName;
    }

    public void setRemoteUserName(String remoteUserName) {
        this.remoteUserName = remoteUserName;
    }

    public String getRemoteUserGroup() {
        return remoteUserGroup;
    }

    public void setRemoteUserGroup(String remoteUserGroup) {
        this.remoteUserGroup = remoteUserGroup;
    }

    public String getRemoteVolumeName() {
        return remoteVolumeName;
    }

    public void setRemoteVolumeName(String remoteVolumeName) {
        this.remoteVolumeName = remoteVolumeName;
    }

    public boolean isPartial() {
        return partial;
    }

    public void setPartial(boolean partial) {
        this.partial = partial;
    }
}
