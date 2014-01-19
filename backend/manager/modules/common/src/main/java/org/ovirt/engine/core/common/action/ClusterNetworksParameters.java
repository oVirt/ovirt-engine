package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class ClusterNetworksParameters extends VdsGroupParametersBase {

    private static final long serialVersionUID = -7719778463146040093L;
    private List<AttachNetworkToVdsGroupParameter> parameters;

    public ClusterNetworksParameters() {
    }

    public ClusterNetworksParameters(Guid clusterId, List<AttachNetworkToVdsGroupParameter> parameters) {
        super(clusterId);
        this.parameters = parameters;
    }

    public List<AttachNetworkToVdsGroupParameter> getClusterNetworksParameters() {
        return parameters;
    }
}
