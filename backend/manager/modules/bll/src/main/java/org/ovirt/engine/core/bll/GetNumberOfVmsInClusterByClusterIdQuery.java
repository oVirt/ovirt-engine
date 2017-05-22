package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.ClusterDao;

public class GetNumberOfVmsInClusterByClusterIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private ClusterDao clusterDao;

    public GetNumberOfVmsInClusterByClusterIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        int numOfVms = clusterDao.getVmsCountByClusterId(getParameters().getId());
        getQueryReturnValue().setReturnValue(numOfVms);
    }
}
