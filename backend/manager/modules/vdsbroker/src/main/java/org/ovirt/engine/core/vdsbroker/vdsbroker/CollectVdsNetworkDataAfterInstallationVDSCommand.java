package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.vdscommands.CollectHostNetworkDataVdsCommandParameters;

public class CollectVdsNetworkDataAfterInstallationVDSCommand extends CollectVdsNetworkDataVDSCommand {

    public CollectVdsNetworkDataAfterInstallationVDSCommand(CollectHostNetworkDataVdsCommandParameters parameters) {
        super(parameters);
    }

    /**
     * After installation, skip the management network since it is can be missing and we will add it afterwards.
     */
    @Override
    protected boolean skipManagementNetwork() {
        return true;
    }

    @Override
    protected void persistCollectedData() {
        super.persistCollectedData();
        VdsDynamic hostFromDb = vdsDynamicDao.get(getVds().getId());
        hostFromDb.setSupportedClusterLevels(getVds().getDynamicData().getSupportedClusterLevels());
        vdsDynamicDao.update(hostFromDb);
    }
}
