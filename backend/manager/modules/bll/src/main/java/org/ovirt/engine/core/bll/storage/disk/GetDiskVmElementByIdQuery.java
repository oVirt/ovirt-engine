package org.ovirt.engine.core.bll.storage.disk;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.VmDeviceIdQueryParameters;
import org.ovirt.engine.core.dao.DiskVmElementDao;

public class GetDiskVmElementByIdQuery <P extends VmDeviceIdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private DiskVmElementDao diskVmElementDao;

    public GetDiskVmElementByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(diskVmElementDao.get(getParameters().getId(),
                getUserID(),
                getParameters().isFiltered()));
    }
}
