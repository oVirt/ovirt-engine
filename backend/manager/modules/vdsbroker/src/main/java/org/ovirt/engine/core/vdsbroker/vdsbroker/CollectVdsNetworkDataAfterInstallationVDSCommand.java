package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.CollectHostNetworkDataVdsCommandParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

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
        DbFacade.getInstance().getVdsDynamicDao().updateIfNeeded(getVds().getDynamicData());
    }
}
