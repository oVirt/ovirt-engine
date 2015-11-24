package org.ovirt.engine.core.bll.storage.domain;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.GetPermittedStorageDomainsByStoragePoolIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetPermittedStorageDomainsByStoragePoolIdQuery<P extends GetPermittedStorageDomainsByStoragePoolIdParameters>
        extends QueriesCommandBase<P> {

    public GetPermittedStorageDomainsByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        P params = getParameters();
        setReturnValue(DbFacade.getInstance()
                .getStorageDomainDao()
                .getPermittedStorageDomainsByStoragePool(getUserID(), params.getActionGroup(), params.getStoragePoolId()));
    }
}
