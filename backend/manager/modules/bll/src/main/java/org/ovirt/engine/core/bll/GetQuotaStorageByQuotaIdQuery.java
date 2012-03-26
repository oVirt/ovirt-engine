package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetQuotaStorageByQuotaIdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetQuotaStorageByQuotaIdQuery<P extends GetQuotaStorageByQuotaIdQueryParameters> extends QueriesCommandBase<P> {
    public GetQuotaStorageByQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getQuotaDAO()
                .getQuotaStorageByQuotaGuidWithGeneralDefault(getParameters().getQuotaId()));
    }

}
