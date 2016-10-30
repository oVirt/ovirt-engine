package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.dao.ClusterDao;

public class GetClusterByNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private ClusterDao clusterDao;

    public GetClusterByNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(clusterDao.getByName(
                getParameters().getName(), getUserID(), getParameters().isFiltered()));
    }
}
