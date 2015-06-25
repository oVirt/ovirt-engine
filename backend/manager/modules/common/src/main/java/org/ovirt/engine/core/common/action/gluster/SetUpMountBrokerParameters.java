package org.ovirt.engine.core.common.action.gluster;

import java.util.Set;

import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.compat.Guid;


public class SetUpMountBrokerParameters extends IdParameters {

    private static final long serialVersionUID = 1L;

    private String remoteVolumeName;
    private String remoteUserName;
    private String remoteUserGroup;
    private boolean partial;
    private Set<Guid> remoteServerIds;

    public SetUpMountBrokerParameters() {
        super();
    }

    public SetUpMountBrokerParameters(Guid clusterId,
            Set<Guid> remoteServerIds,
            String remoteVolumeName,
            String remoteUserName) {
        this(clusterId, remoteServerIds, remoteVolumeName, remoteUserName, remoteUserName, true);
    }

    public SetUpMountBrokerParameters(Guid clusterId, Set<Guid> remoteServerIds,
            String remoteVolumeName,
            String remoteUserName,
            String remoteUserGroup) {
        this(clusterId, remoteServerIds, remoteVolumeName, remoteUserName, remoteUserGroup, false);
    }

    public SetUpMountBrokerParameters(Guid clusterId, Set<Guid> remoteServerIds, String remoteVolumeName,
            String remoteUserName,
            String remoteUserGroup,
            boolean partial) {
        super(clusterId);
        this.remoteServerIds = remoteServerIds;
        this.remoteVolumeName = remoteVolumeName;
        this.remoteUserName = remoteUserName;
        this.remoteUserGroup = remoteUserGroup;
        this.partial = partial;
    }

    public Set<Guid> getRemoteServerIds() {
        return remoteServerIds;
    }

    public void setRemoteServerIds(Set<Guid> remoteServerIds) {
        this.remoteServerIds = remoteServerIds;
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
