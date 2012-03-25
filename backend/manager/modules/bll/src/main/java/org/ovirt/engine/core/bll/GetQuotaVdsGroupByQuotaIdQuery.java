package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetQuotaVdsGroupByQuotaIdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetQuotaVdsGroupByQuotaIdQuery<P extends GetQuotaVdsGroupByQuotaIdQueryParameters> extends QueriesCommandBase<P> {
    public GetQuotaVdsGroupByQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getQuotaDAO()
                .getQuotaVdsGroupByQuotaGuidWithGeneralDefault(getParameters().getQuotaId()));
    }
}
