package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.vdscommands.CollectHostNetworkDataVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UserConfiguredNetworkData;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class CollectVdsNetworkDataVDSCommand extends GetCapabilitiesVDSCommand<CollectHostNetworkDataVdsCommandParameters> {
    @Inject
    protected VdsDynamicDao vdsDynamicDao;
    @Inject
    private HostNetworkTopologyPersister hostNetworkTopologyPersister;

    public CollectVdsNetworkDataVDSCommand(CollectHostNetworkDataVdsCommandParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        // call getVdsCapabilities verb
        super.executeVdsBrokerCommand();

        TransactionSupport.executeInNewTransaction(() -> {
            persistCollectedData();
            return null;
        });

        proceedProxyReturnValue();
    }

    protected void persistCollectedData() {
        updateNetConfigDirtyFlag();
        hostNetworkTopologyPersister.persistAndEnforceNetworkCompliance(getVds(),
                skipManagementNetwork(),
                new UserConfiguredNetworkData());
    }

    /**
     * @return By default, don't skip the management network check.
     */
    protected boolean skipManagementNetwork() {
        return false;
    }

    /**
     * Update the {@link org.ovirt.engine.core.common.businessentities.VdsDynamic#getNetConfigDirty()} field in the
     * DB.<br>
     * The update is done in a new transaction since we don't care if afterwards something goes wrong, but we would like
     * to minimize races with other command that update the
     * {@link org.ovirt.engine.core.common.businessentities.VdsDynamic} entity in the DB.
     */
    private void updateNetConfigDirtyFlag() {
        TransactionSupport.executeInNewTransaction(() -> {
            vdsDynamicDao.updateNetConfigDirty(getVds().getId(), getVds().getNetConfigDirty());
            return null;
        });
    }
}
