package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.QuotaDao;

public class GetQuotaStorageByQuotaIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private QuotaDao quotaDao;

    public GetQuotaStorageByQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                quotaDao.getQuotaStorageByQuotaGuidWithGeneralDefault(getParameters().getId()));
    }
}
