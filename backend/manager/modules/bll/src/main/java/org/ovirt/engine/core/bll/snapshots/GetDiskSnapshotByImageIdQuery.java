package org.ovirt.engine.core.bll.snapshots;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.DiskImageDao;

public class GetDiskSnapshotByImageIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private DiskImageDao diskImageDao;

    public GetDiskSnapshotByImageIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(diskImageDao.getSnapshotById(getParameters().getId()));
    }
}
