package org.ovirt.engine.core.bll.snapshots;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.SnapshotDao;

/**
 * Return a list of all the snapshots for the given VM id.<br>
 * The snapshots are sorted by their creation date.<br>
 * The snapshots don't contain their configuration, since this is parsed and returned as a VM object.
 */
public class GetAllVmSnapshotsByVmIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    @Inject
    private SnapshotDao snapshotDao;

    public GetAllVmSnapshotsByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<Snapshot> snapshotsList =
                snapshotDao.getAll(getParameters().getId(), getUserID(), getParameters().isFiltered());
        getQueryReturnValue().setReturnValue(snapshotsList);
    }
}
