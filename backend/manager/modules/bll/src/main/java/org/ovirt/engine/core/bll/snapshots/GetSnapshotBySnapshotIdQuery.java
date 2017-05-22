package org.ovirt.engine.core.bll.snapshots;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.SnapshotDao;

public class GetSnapshotBySnapshotIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private SnapshotDao snapshotDao;


    public GetSnapshotBySnapshotIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Snapshot snapshot = snapshotDao.get(getParameters().getId(), getUserID(), getParameters().isFiltered());
        if (snapshot != null) {
            getQueryReturnValue().setReturnValue(snapshot);
        }
    }
}
