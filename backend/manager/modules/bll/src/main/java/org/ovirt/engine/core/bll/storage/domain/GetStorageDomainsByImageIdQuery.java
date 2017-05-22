package org.ovirt.engine.core.bll.storage.domain;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.StorageDomainDao;

public class GetStorageDomainsByImageIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private StorageDomainDao storageDomainDao;

    public GetStorageDomainsByImageIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(storageDomainDao.getAllStorageDomainsByImageId(getParameters().getId()));
    }
}
