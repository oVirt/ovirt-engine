package org.ovirt.engine.core.bll.storage.domain;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetPermittedStorageDomainsByStoragePoolIdParameters;
import org.ovirt.engine.core.dao.StorageDomainDao;

public class GetPermittedStorageDomainsByStoragePoolIdQuery<P extends GetPermittedStorageDomainsByStoragePoolIdParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private StorageDomainDao storageDomainDao;

    public GetPermittedStorageDomainsByStoragePoolIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        P params = getParameters();
        setReturnValue(storageDomainDao
                .getPermittedStorageDomainsByStoragePool(getUserID(), params.getActionGroup(), params.getStoragePoolId()));
    }
}
