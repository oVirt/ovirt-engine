package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetDataCentersWithPermittedActionOnClustersQuery<P extends GetEntitiesWithPermittedActionParameters>
        extends QueriesCommandBase<P> {

    public GetDataCentersWithPermittedActionOnClustersQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        P params = getParameters();
        Guid userId = SessionDataContainer.getInstance().getUser(params.getSessionId()).getUserId();
        setReturnValue(DbFacade.getInstance().getStoragePoolDAO().
                getDataCentersWithPermittedActionOnClusters(userId, params.getActionGroup()));
    }
}
