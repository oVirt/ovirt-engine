package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetDataCentersWithPermittedActionOnClustersQuery<P extends GetEntitiesWithPermittedActionParameters>
        extends QueriesCommandBase<P> {

    public GetDataCentersWithPermittedActionOnClustersQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        P params = getParameters();
        setReturnValue(DbFacade.getInstance().getStoragePoolDAO().
                getDataCentersWithPermittedActionOnClusters(getUserID(), params.getActionGroup()));
    }
}
