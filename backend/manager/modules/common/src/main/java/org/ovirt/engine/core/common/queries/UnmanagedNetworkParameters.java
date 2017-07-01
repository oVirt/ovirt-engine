package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class UnmanagedNetworkParameters extends QueryParametersBase {

    private static final long serialVersionUID = 3874444912691547792L;
    private Guid hostId;
    private String networkName;

    public UnmanagedNetworkParameters() {
    }

    public UnmanagedNetworkParameters(Guid hostId, String networkName) {
        this.hostId = hostId;
        this.networkName = networkName;
    }

    public Guid getHostId() {
        return hostId;
    }

    public String getNetworkName() {
        return networkName;
    }
}
