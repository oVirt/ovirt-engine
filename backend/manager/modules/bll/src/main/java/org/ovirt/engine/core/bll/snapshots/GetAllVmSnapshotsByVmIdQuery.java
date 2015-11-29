package org.ovirt.engine.core.bll.snapshots;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

/**
 * Return a list of all the snapshots for the given VM id.<br>
 * The snapshots are sorted by their creation date.<br>
 * The snapshots don't contain their configuration, since this is parsed and returned as a VM object.
 */
public class GetAllVmSnapshotsByVmIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetAllVmSnapshotsByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<Snapshot> snapshotsList = getDbFacade().getSnapshotDao()
                .getAll(getParameters().getId(), getUserID(), getParameters().isFiltered());
        getQueryReturnValue().setReturnValue(snapshotsList);
    }
}
