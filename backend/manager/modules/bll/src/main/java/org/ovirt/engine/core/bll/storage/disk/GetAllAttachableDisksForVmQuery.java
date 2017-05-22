package org.ovirt.engine.core.bll.storage.disk;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetAllAttachableDisksForVmQueryParameters;
import org.ovirt.engine.core.dao.DiskDao;

public class GetAllAttachableDisksForVmQuery<P extends GetAllAttachableDisksForVmQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private DiskDao diskDao;

    public GetAllAttachableDisksForVmQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(diskDao.getAllAttachableDisksByPoolId(getParameters().getStoragePoolId(),
                        getParameters().getVmId(),
                        getUserID(),
                        getParameters().isFiltered()));
    }
}
