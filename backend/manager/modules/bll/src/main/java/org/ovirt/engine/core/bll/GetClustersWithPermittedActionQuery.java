package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.dao.ClusterDao;

public class GetClustersWithPermittedActionQuery<P extends GetEntitiesWithPermittedActionParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private ClusterDao clusterDao;

    public GetClustersWithPermittedActionQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        P params = getParameters();
        setReturnValue(clusterDao.getClustersWithPermittedAction(getUserID(), params.getActionGroup()));
    }
}
