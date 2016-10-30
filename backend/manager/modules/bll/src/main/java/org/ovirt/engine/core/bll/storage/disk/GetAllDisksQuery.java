package org.ovirt.engine.core.bll.storage.disk;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.DiskDao;

public class GetAllDisksQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private DiskDao diskDao;

    public GetAllDisksQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(diskDao.getAll(getUserID(), getParameters().isFiltered()));
    }
}
