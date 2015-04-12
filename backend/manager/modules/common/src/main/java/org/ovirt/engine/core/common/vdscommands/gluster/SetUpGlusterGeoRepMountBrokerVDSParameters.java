package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class SetUpGlusterGeoRepMountBrokerVDSParameters extends VdsIdVDSCommandParametersBase {
    private String remoteUserName;
    private String remoteGroupName;
    private String remoteVolumeName;
    private boolean partial;

    public SetUpGlusterGeoRepMountBrokerVDSParameters() {

    }

    public SetUpGlusterGeoRepMountBrokerVDSParameters(Guid vdsId, String userName, String userGroup, String remoteVolumeName) {
        this(vdsId, userName, userGroup, remoteVolumeName, false);
    }

    public SetUpGlusterGeoRepMountBrokerVDSParameters(Guid vdsId,
            String remoteUserName,
            String remoteGroupName,
            String remoteVolumeName,
            boolean partial) {
        super(vdsId);
        this.remoteUserName = remoteUserName;
        this.remoteGroupName = remoteGroupName;
        this.remoteVolumeName = remoteVolumeName;
        this.partial = partial;
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

    public boolean isPartial() {
        return partial;
    }

    public void setPartial(boolean partial) {
        this.partial = partial;
    }
}
