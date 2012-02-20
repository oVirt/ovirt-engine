package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetStorageDomainByIdQuery<P extends StorageDomainQueryParametersBase> extends QueriesCommandBase<P> {
    public GetStorageDomainByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getStorageDomainDAO().get(getParameters().getStorageDomainId(),
                        getUserID(),
                        getParameters().isFiltered()));
    }
}
