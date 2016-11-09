package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.snapshots.SnapshotVmConfigurationHelper;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.SnapshotDao;

public class GetVmNextRunConfigurationQuery<P extends IdQueryParameters> extends GetVmByVmIdQuery<P> {

    @Inject
    protected SnapshotVmConfigurationHelper snapshotVmConfigurationHelper;

    @Inject
    private SnapshotDao snapshotDao;

    public GetVmNextRunConfigurationQuery(P parameters) {
        super(parameters);
    }

    public GetVmNextRunConfigurationQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Snapshot snapshot = snapshotDao.get(getParameters().getId(), Snapshot.SnapshotType.NEXT_RUN, getUserID(), getParameters().isFiltered());

        if (snapshot != null) {
            VM vm = snapshotVmConfigurationHelper.getVmFromConfiguration(
                    snapshot.getVmConfiguration(), snapshot.getVmId(), snapshot.getId());

            // update information that is not saved in the config
            vmHandler.updateDisksFromDb(vm);
            vmHandler.updateVmGuestAgentVersion(vm);
            vmHandler.updateNetworkInterfacesFromDb(vm);
            vmHandler.updateVmStatistics(vm);

            getQueryReturnValue().setReturnValue(vm);
        } else {
            // in case no next_run return static configuration
            super.executeQueryCommand();
        }
    }
}
