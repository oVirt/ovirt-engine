package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Guid;

public class ManagementNetworkOnClusterOperationParameters extends VdsGroupOperationParameters {

    private static final long serialVersionUID = 1L;

    private Guid managementNetworkId;

    public ManagementNetworkOnClusterOperationParameters(VDSGroup cluster) {
        this(cluster, null);
    }

    public ManagementNetworkOnClusterOperationParameters(VDSGroup cluster, Guid managementNetworkId) {
        super(cluster);

        this.managementNetworkId = managementNetworkId;
    }

    public Guid getManagementNetworkId() {
        return managementNetworkId;
    }

    public ManagementNetworkOnClusterOperationParameters() {
    }
}
