package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.vdscommands.CollectHostNetworkDataVdsCommandParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDynamicDao;

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
        VdsDynamicDao vdsDynamicDao = DbFacade.getInstance().getVdsDynamicDao();
        VdsDynamic hostFromDb = vdsDynamicDao.get(getVds().getId());
        hostFromDb.setSupportedClusterLevels(getVds().getDynamicData().getSupportedClusterLevels());
        vdsDynamicDao.update(hostFromDb);
    }
}
