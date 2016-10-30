package org.ovirt.engine.core.bll.storage.domain;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.StorageDomainDao;

public class GetStorageDomainsByImageIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private StorageDomainDao storageDomainDao;

    public GetStorageDomainsByImageIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(storageDomainDao.getAllStorageDomainsByImageId(getParameters().getId()));
    }
}
