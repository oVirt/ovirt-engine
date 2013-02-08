package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetDataCentersWithPermittedActionOnClustersParameters;

public class GetDataCentersWithPermittedActionOnClustersQuery<P extends GetDataCentersWithPermittedActionOnClustersParameters>
        extends QueriesCommandBase<P> {

    public GetDataCentersWithPermittedActionOnClustersQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        P params = getParameters();
        setReturnValue(getDbFacade().getStoragePoolDao().
                getDataCentersWithPermittedActionOnClusters(getUserID(), params.getActionGroup(),
                        params.isSupportsVirtService(), params.isSupportsGlusterService()));
    }
}
