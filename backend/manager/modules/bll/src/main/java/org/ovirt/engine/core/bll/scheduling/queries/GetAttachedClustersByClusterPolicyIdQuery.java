package org.ovirt.engine.core.bll.scheduling.queries;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VdsGroupDao;

public class GetAttachedClustersByClusterPolicyIdQuery extends QueriesCommandBase<IdQueryParameters> {
    public GetAttachedClustersByClusterPolicyIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Inject
    private VdsGroupDao vdsGroupDao;

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                vdsGroupDao.getClustersByClusterPolicyId(getParameters().getId()));
    }
}
