package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.MacPoolDao;

public class GetMacPoolByIdQuery extends QueriesCommandBase<IdQueryParameters> {
    @Inject
    private MacPoolDao macPoolDao;

    public GetMacPoolByIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final MacPool macPool = macPoolDao.get(getParameters().getId());

        getQueryReturnValue().setReturnValue(macPool);
    }
}
