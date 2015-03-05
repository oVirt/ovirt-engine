package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.compat.Guid;

public class SetUpMountBrokerParameters extends IdParameters {

    private static final long serialVersionUID = 1L;

    private String remoteVolumeName;
    private String remoteUserName;
    private String remoteUserGroup;

    public SetUpMountBrokerParameters() {
        super();
    }

    public SetUpMountBrokerParameters(Guid remoteHostId, String remoteVolumeName, String remoteUserName, String remoteUserGroup) {
        super(remoteHostId);
        this.remoteUserName = remoteUserName;
        this.remoteUserGroup = remoteUserGroup;
        this.setRemoteVolumeName(remoteVolumeName);
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
}
