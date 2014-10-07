package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.vdscommands.CollectHostNetworkDataVdsCommandParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class CollectVdsNetworkDataVDSCommand extends GetCapabilitiesVDSCommand<CollectHostNetworkDataVdsCommandParameters> {

    private final HostNetworkTopologyPersister hostNetworkTopologyPersister;

    public CollectVdsNetworkDataVDSCommand(CollectHostNetworkDataVdsCommandParameters parameters) {
        super(parameters);
        hostNetworkTopologyPersister = HostNetworkTopologyPersisterImpl.getInstance();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        // call getVdsCapabilities verb
        super.executeVdsBrokerCommand();
        persistCollectedData();

        proceedProxyReturnValue();
    }

    protected void persistCollectedData() {
        updateNetConfigDirtyFlag();
        hostNetworkTopologyPersister.persistAndEnforceNetworkCompliance(getVds(),
                skipManagementNetwork(),
                Entities.entitiesByName(getParameters().getInterfaces()));
    }

    /**
     * @return By default, don't skip the management network check.
     */
    protected boolean skipManagementNetwork() {
        return false;
    }

    /**
     * Update the {@link VdsDynamic#getnet_config_dirty()} field in the DB.<br>
     * The update is done in a new transaction since we don't care if afterwards something goes wrong, but we would like
     * to minimize races with other command that update the {@link VdsDynamic} entity in the DB.
     */
    private void updateNetConfigDirtyFlag() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                DbFacade.getInstance()
                        .getVdsDynamicDao()
                        .updateNetConfigDirty(getVds().getId(), getVds().getNetConfigDirty());
                return null;
            }
        });
    }
}
