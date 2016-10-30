package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.ClusterDao;

public class IsClusterEmptyQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private ClusterDao clusterDao;

    public IsClusterEmptyQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Boolean isEmpty = clusterDao.getIsEmpty(getParameters().getId());

        getQueryReturnValue().setReturnValue(isEmpty);
    }
}
