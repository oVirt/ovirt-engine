package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.QuotaDao;

public class GetQuotaByQuotaIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private QuotaDao quotaDao;

    public GetQuotaByQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(quotaDao.getById(getParameters().getId()));
    }
}
