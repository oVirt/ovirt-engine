package org.ovirt.engine.core.bll.storage.domain;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.StorageDomainAndPoolQueryParameters;
import org.ovirt.engine.core.dao.StorageDomainDao;

public class GetStorageDomainByIdAndStoragePoolIdQuery<P extends StorageDomainAndPoolQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private StorageDomainDao storageDomainDao;

    public GetStorageDomainByIdAndStoragePoolIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                storageDomainDao.getForStoragePool(getParameters().getStorageDomainId(),
                        getParameters().getStoragePoolId()));
    }
}
