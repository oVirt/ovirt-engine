package org.ovirt.engine.core.bll.snapshots;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.SnapshotDao;

/**
 * Returns a list of all snapshots for a specified VM ID including configuration data.
 */
public class GetAllVmSnapshotsFromConfigurationByVmIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetAllVmSnapshotsFromConfigurationByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        SnapshotVmConfigurationHelper snapshotVmConfigurationHelper = getSnapshotVmConfigurationHelper();
        List<Snapshot> snapshots = getSnapshotDao().getAllWithConfiguration(getParameters().getId());
        for (Snapshot snapshot : snapshots) {
            VM vm = snapshotVmConfigurationHelper.getVmFromConfiguration(
                    snapshot.getVmConfiguration(), snapshot.getVmId(), snapshot.getId());
            if (vm != null) {
                snapshot.setDiskImages(vm.getImages());
            }
        }
        getQueryReturnValue().setReturnValue(snapshots);
    }

    protected SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
    }

    protected SnapshotVmConfigurationHelper getSnapshotVmConfigurationHelper() {
        return new SnapshotVmConfigurationHelper();
    }
}
