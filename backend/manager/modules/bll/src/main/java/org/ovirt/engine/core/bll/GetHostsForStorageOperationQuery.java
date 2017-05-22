package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetHostsForStorageOperationParameters;
import org.ovirt.engine.core.dao.VdsDao;

public class GetHostsForStorageOperationQuery<P extends GetHostsForStorageOperationParameters> extends QueriesCommandBase<P> {
    @Inject
    private VdsDao vdsDao;

    public GetHostsForStorageOperationQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(vdsDao.getHostsForStorageOperation(
                getParameters().getId(), getParameters().isLocalFsOnly()));
    }
}
