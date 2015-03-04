package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class SetUpGlusterGeoRepMountBrokerVDSParameters extends VdsIdVDSCommandParametersBase {
    private String remoteUserName;
    private String remoteGroupName;
    private String remoteVolumeName;

    public SetUpGlusterGeoRepMountBrokerVDSParameters() {

    }

    public SetUpGlusterGeoRepMountBrokerVDSParameters(Guid vdsId, String userName, String userGroup, String remoteVolumeName) {
        super(vdsId);
        this.remoteUserName = userName;
        this.remoteGroupName = userGroup;
        this.remoteVolumeName = remoteVolumeName;
    }

    public String getRemoteUserName() {
        return remoteUserName;
    }

    public void setRemoteUserName(String remoteUserName) {
        this.remoteUserName = remoteUserName;
    }

    public String getRemoteGroupName() {
        return remoteGroupName;
    }

    public void setRemoteGroupName(String userGroup) {
        this.remoteGroupName = userGroup;
    }

    public String getRemoteVolumeName() {
        return remoteVolumeName;
    }

    public void setRemoteVolumeName(String remoteVolumeName) {
        this.remoteVolumeName = remoteVolumeName;
    }
}
