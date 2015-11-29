package org.ovirt.engine.core.bll.snapshots;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.SnapshotDao;

public class GetSnapshotBySnapshotIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {


    public GetSnapshotBySnapshotIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Snapshot snapshot = getSnapshotDao().get(getParameters().getId(), getUserID(), getParameters().isFiltered());
        if (snapshot != null) {
            getQueryReturnValue().setReturnValue(snapshot);
        }
    }

    protected SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
    }
}
