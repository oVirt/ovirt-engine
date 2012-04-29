package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;

public class GetDataCentersWithPermittedActionOnClustersQuery<P extends GetEntitiesWithPermittedActionParameters>
        extends QueriesCommandBase<P> {

    public GetDataCentersWithPermittedActionOnClustersQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        P params = getParameters();
        setReturnValue(getDbFacade().getStoragePoolDAO().
                getDataCentersWithPermittedActionOnClusters(getUserID(), params.getActionGroup()));
    }
}
