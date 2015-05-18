package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

import javax.inject.Inject;
import java.util.List;

public class GetQuotasConsumptionForCurrentUserQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    @Inject
    private QuotaManager quotaManager;

    public GetQuotasConsumptionForCurrentUserQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<Quota> quotaList = getDbFacade().getQuotaDao().getQuotaByAdElementId(getUser().getId(), null, true);
        getQueryReturnValue().setReturnValue(quotaManager.generatePerUserUsageReport(quotaList));
    }
}
