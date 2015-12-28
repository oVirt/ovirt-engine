package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class ClusterNetworksParameters extends ClusterParametersBase {

    private static final long serialVersionUID = -7719778463146040093L;
    private List<AttachNetworkToClusterParameter> parameters;

    public ClusterNetworksParameters() {
    }

    public ClusterNetworksParameters(Guid clusterId, List<AttachNetworkToClusterParameter> parameters) {
        super(clusterId);
        this.parameters = parameters;
    }

    public List<AttachNetworkToClusterParameter> getClusterNetworksParameters() {
        return parameters;
    }
}
