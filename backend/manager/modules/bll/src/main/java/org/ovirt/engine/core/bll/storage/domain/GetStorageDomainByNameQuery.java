package org.ovirt.engine.core.bll.storage.domain;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;

public class GetStorageDomainByNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;

    public GetStorageDomainByNameQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                storageDomainStaticDao.getByName(getParameters().getName(), getUserID(), getParameters().isFiltered()));
    }
}
