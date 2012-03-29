package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetAllRelevantQuotasForVdsGroupParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetAllRelevantQuotasForVdsGroupQuery<P extends GetAllRelevantQuotasForVdsGroupParameters> extends QueriesCommandBase<P> {
    public GetAllRelevantQuotasForVdsGroupQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getQuotaDAO()
                .getAllRelevantQuotasForVdsGroup(getParameters().getVdsGroupId()));
    }
}
