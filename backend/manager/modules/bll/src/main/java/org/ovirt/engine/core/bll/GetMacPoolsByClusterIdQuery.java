package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.MacPoolDao;

public class GetMacPoolsByClusterIdQuery extends QueriesCommandBase<IdQueryParameters> {

    @Inject
    private MacPoolDao macPoolDao;

    public GetMacPoolsByClusterIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        MacPool macPool = macPoolDao.getByClusterId(getParameters().getId());
        getQueryReturnValue().setReturnValue(macPool);
    }
}
