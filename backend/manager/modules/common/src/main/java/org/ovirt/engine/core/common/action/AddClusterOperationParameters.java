package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Guid;

public class AddClusterOperationParameters extends VdsGroupOperationParameters {

    private static final long serialVersionUID = 1L;

    private Guid managementNetworkId;

    public AddClusterOperationParameters(VDSGroup cluster) {
        this(cluster, null);
    }

    public AddClusterOperationParameters(VDSGroup cluster, Guid managementNetworkId) {
        super(cluster);

        this.managementNetworkId = managementNetworkId;
    }

    public Guid getManagementNetworkId() {
        return managementNetworkId;
    }

    public AddClusterOperationParameters() {
    }
}
