package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class AttachNetworkClusterValidator extends NetworkClusterValidatorBase {

    public AttachNetworkClusterValidator(InterfaceDao interfaceDao,
            NetworkDao networkDao,
            VdsDao vdsDao,
            NetworkCluster networkCluster) {
        super(interfaceDao, networkDao, vdsDao, networkCluster);
    }

    @Override
    protected boolean isManagementNetworkChanged() {
        return networkCluster.isManagement();
    }
}
