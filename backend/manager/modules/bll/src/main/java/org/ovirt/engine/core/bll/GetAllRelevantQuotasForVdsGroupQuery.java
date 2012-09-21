package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetAllRelevantQuotasForVdsGroupParameters;

public class GetAllRelevantQuotasForVdsGroupQuery<P extends GetAllRelevantQuotasForVdsGroupParameters> extends QueriesCommandBase<P> {
    public GetAllRelevantQuotasForVdsGroupQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade()
                .getQuotaDao()
                .getAllRelevantQuotasForVdsGroup(getParameters().getVdsGroupId(),
                        getUserID(),
                        getParameters().isFiltered()));
    }
}
