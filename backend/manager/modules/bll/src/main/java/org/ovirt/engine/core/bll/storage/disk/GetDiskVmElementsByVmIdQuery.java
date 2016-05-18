package org.ovirt.engine.core.bll.storage.disk;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.DiskVmElementDao;

public class GetDiskVmElementsByVmIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private DiskVmElementDao diskVmElementDao;

    public GetDiskVmElementsByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(diskVmElementDao.getAllForVm(getParameters().getId()));
    }
}
