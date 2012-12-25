package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

import java.util.List;

public class GetQuotasConsumptionForCurrentUserQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetQuotasConsumptionForCurrentUserQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<Quota> quotaList = getDbFacade().getQuotaDao().getQuotaByAdElementId(getUser().getUserId(), null, true);
        getQueryReturnValue().setReturnValue(QuotaManager.getInstance().generatePerUserUsageReport(quotaList));
    }
}
