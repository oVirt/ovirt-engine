package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.dao.ClusterDao;

public class GetClusterByNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private ClusterDao clusterDao;

    public GetClusterByNameQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(clusterDao.getByName(
                getParameters().getName(), getUserID(), getParameters().isFiltered()));
    }
}
