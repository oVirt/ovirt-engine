package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetQuotaByQuotaIdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetQuotaByQuotaIdQuery<P extends GetQuotaByQuotaIdQueryParameters> extends QueriesCommandBase<P> {

    public GetQuotaByQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getQuotaDAO()
                .getById(getParameters().getQuotaId()));
    }
}
