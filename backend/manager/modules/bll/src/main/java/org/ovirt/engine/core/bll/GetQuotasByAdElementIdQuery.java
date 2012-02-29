package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetQuotasByAdElementIdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetQuotasByAdElementIdQuery<P extends GetQuotasByAdElementIdQueryParameters> extends QueriesCommandBase<P> {
    public GetQuotasByAdElementIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getQuotaDAO()
                .getQuotaByAdElementId(getParameters().getAdElementId(), getParameters().getStoragePoolId()));
    }
}
