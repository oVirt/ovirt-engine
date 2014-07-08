package org.ovirt.engine.core.bll.profiles;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetDiskProfilesByStorageDomainIdQuery extends QueriesCommandBase<IdQueryParameters> {

    public GetDiskProfilesByStorageDomainIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getDiskProfileDao()
                .getAllForStorageDomain(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }

}
