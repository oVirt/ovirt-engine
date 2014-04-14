package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.snapshots.SnapshotVmConfigurationHelper;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.SnapshotDao;

public class GetVmNextRunConfigurationQuery<P extends IdQueryParameters> extends GetVmByVmIdQuery<P> {

    public GetVmNextRunConfigurationQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        SnapshotVmConfigurationHelper snapshotVmConfigurationHelper = getSnapshotVmConfigurationHelper();
        Snapshot snapshot = getSnapshotDao().get(getParameters().getId(), Snapshot.SnapshotType.NEXT_RUN, getUserID(), getParameters().isFiltered());

        if (snapshot != null) {
            VM vm = snapshotVmConfigurationHelper.getVmFromConfiguration(
                    snapshot.getVmConfiguration(), snapshot.getVmId(), snapshot.getId());

            // update information that is not saved in the config
            VmHandler.updateDisksFromDb(vm);
            VmHandler.updateVmGuestAgentVersion(vm);
            VmHandler.updateNetworkInterfacesFromDb(vm);

            getQueryReturnValue().setReturnValue(vm);
        } else {
            // in case no next_run return static configuration
            super.executeQueryCommand();
        }
    }

    protected SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
    }

    protected SnapshotVmConfigurationHelper getSnapshotVmConfigurationHelper() {
        return new SnapshotVmConfigurationHelper();
    }

}
