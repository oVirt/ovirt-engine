package org.ovirt.engine.core.bll.scheduling.queries;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.ClusterDao;

public class GetAttachedClustersByClusterPolicyIdQuery extends QueriesCommandBase<IdQueryParameters> {
    public GetAttachedClustersByClusterPolicyIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Inject
    private ClusterDao clusterDao;

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                clusterDao.getClustersByClusterPolicyId(getParameters().getId()));
    }
}
