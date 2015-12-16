package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.snapshots.SnapshotVmConfigurationHelper;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.SnapshotDao;

/**
 * This class implements the logic of the query responsible for getting a VM configuration by snapshot.
 */
public class GetVmConfigurationBySnapshotQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetVmConfigurationBySnapshotQuery(P parameters) {
        super(parameters);
    }

    public GetVmConfigurationBySnapshotQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        SnapshotVmConfigurationHelper snapshotVmConfigurationHelper = getSnapshotVmConfigurationHelper();
        Snapshot snapshot = getSnapshotDao().get(getParameters().getId(), getUserID(), getParameters().isFiltered());
        VM vm = null;

        if (snapshot == null) {
            log.warn("Snapshot '{}' does not exist", getParameters().getId());
        }
        else {
            vm = snapshotVmConfigurationHelper.getVmFromConfiguration(
                    snapshot.getVmConfiguration(), snapshot.getVmId(), snapshot.getId());
        }

        getQueryReturnValue().setReturnValue(vm);
    }

    protected SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
    }

    protected SnapshotVmConfigurationHelper getSnapshotVmConfigurationHelper() {
        return new SnapshotVmConfigurationHelper();
    }

}
