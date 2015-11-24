package org.ovirt.engine.core.bll.storage.domain;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.NameQueryParameters;

public class GetStorageDomainByNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {
    public GetStorageDomainByNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getStorageDomainStaticDao().getByName(getParameters().getName(),
                        getUserID(),
                        getParameters().isFiltered()));
    }
}
