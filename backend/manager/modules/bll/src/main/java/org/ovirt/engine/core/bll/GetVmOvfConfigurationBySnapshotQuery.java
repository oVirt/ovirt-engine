package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.SnapshotDao;

public class GetVmOvfConfigurationBySnapshotQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {


    public GetVmOvfConfigurationBySnapshotQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Snapshot snapshot = getSnapshotDao().get(getParameters().getId(), getUserID(), getParameters().isFiltered());
        if (snapshot != null) {
            getQueryReturnValue().setReturnValue(snapshot.getVmConfiguration());
        }
    }

    protected SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
    }
}
