package org.ovirt.engine.core.common.action.gluster;

import java.util.Set;

import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.compat.Guid;

public class SetUpPasswordLessSSHParameters extends IdParameters {

    private static final long serialVersionUID = 1L;

    private Set<Guid> destinationHostIds;

    private String userName;

    public SetUpPasswordLessSSHParameters() {
        super();
    }

    public SetUpPasswordLessSSHParameters(Guid clusterId, Set<Guid> destinationHostIds, String userName) {
        super(clusterId);
        this.destinationHostIds = destinationHostIds;
        this.userName = userName;
    }

    public Set<Guid> getDestinationHostIds() {
        return destinationHostIds;
    }

    public void setDestinationHostIds(Set<Guid> destinationHostIds) {
        this.destinationHostIds = destinationHostIds;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
