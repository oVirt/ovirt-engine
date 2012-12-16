package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetQuotasConsumptionForCurrentUserQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.GuidUtils;

import java.util.ArrayList;
import java.util.List;

public class GetQuotasConsumptionForCurrentUserQuery<P extends GetQuotasConsumptionForCurrentUserQueryParameters> extends QueriesCommandBase<P> {
    public GetQuotasConsumptionForCurrentUserQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vms = getDbFacade().getVmDao().getAllForUser(getUser().getUserId());

        List<Guid> ids = new ArrayList<Guid>();
        ids.add(getUser().getUserId());
        ids.addAll(GuidUtils.getGuidListFromString(getUser().getGroupIds()));
        List<Quota> quotaList = new ArrayList<Quota>();
        for (Guid id : ids) {
            quotaList.addAll(getDbFacade()
                    .getQuotaDao()
                    .getQuotaByAdElementId(id, getParameters().getStoragePoolId()));
        }
        getQueryReturnValue().setReturnValue(QuotaManager.getInstance().generatePerUserUsageReport(quotaList, vms));
    }
}
