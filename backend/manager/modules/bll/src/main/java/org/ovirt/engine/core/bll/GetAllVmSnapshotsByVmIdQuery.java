package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByVmIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * Return a list of all the snapshots for the given VM id.<br>
 * The snapshots are sorted by their creation date.<br>
 * The snapshots don't contain their configuration, since this is parsed and returned as a VM object.
 */
public class GetAllVmSnapshotsByVmIdQuery<P extends GetAllVmSnapshotsByVmIdParameters>
        extends QueriesCommandBase<P> {
    public GetAllVmSnapshotsByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<Snapshot> snapshotsList = DbFacade.getInstance().getSnapshotDao()
                .getAll(getParameters().getVmId(), getUserID(), getParameters().isFiltered());
        getQueryReturnValue().setReturnValue(snapshotsList);
    }
}
