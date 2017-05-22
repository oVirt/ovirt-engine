package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetQuotasByAdElementIdQueryParameters;
import org.ovirt.engine.core.dao.QuotaDao;

public class GetQuotasByAdElementIdQuery<P extends GetQuotasByAdElementIdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private QuotaDao quotaDao;

    public GetQuotasByAdElementIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(quotaDao
                .getQuotaByAdElementId(getParameters().getAdElementId(), getParameters().getStoragePoolId(), false));
    }
}
