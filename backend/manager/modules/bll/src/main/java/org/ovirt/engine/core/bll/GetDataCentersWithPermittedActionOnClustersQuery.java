package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetDataCentersWithPermittedActionOnClustersParameters;
import org.ovirt.engine.core.dao.StoragePoolDao;

public class GetDataCentersWithPermittedActionOnClustersQuery<P extends GetDataCentersWithPermittedActionOnClustersParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private StoragePoolDao storagePoolDao;

    public GetDataCentersWithPermittedActionOnClustersQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        P params = getParameters();
        setReturnValue(storagePoolDao.
                getDataCentersWithPermittedActionOnClusters(getUserID(), params.getActionGroup(),
                        params.isSupportsVirtService(), params.isSupportsGlusterService()));
    }
}
