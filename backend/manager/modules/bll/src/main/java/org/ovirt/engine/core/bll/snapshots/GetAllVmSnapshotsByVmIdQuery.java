package org.ovirt.engine.core.bll.snapshots;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.SnapshotDao;

/**
 * Return a list of all, but next-run, snapshots for the given VM id.<br>
 * The snapshots are sorted by their creation date.<br>
 * The snapshots don't contain their configuration, since this is parsed and returned as a VM object.
 */
public class GetAllVmSnapshotsByVmIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    @Inject
    private SnapshotDao snapshotDao;

    public GetAllVmSnapshotsByVmIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        var snapshots = snapshotDao.getAll(getParameters().getId(), getUserID(), getParameters().isFiltered());
        snapshots = snapshots.stream()
                .filter(snapshot -> snapshot.getType() != SnapshotType.NEXT_RUN)
                .collect(Collectors.toList());
        getQueryReturnValue().setReturnValue(snapshots);
    }
}
