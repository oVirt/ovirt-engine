package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetAllRelevantQuotasForStorageParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetAllRelevantQuotasForStorageQuery<P extends GetAllRelevantQuotasForStorageParameters> extends QueriesCommandBase<P> {
    public GetAllRelevantQuotasForStorageQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getQuotaDAO()
                .getAllRelevantQuotasForStorage(getParameters().getStorageId()));
    }
}
