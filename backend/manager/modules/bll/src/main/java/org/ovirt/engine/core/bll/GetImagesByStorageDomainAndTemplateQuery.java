package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.StorageDomainTemplateQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetImagesByStorageDomainAndTemplateQuery<P extends StorageDomainTemplateQueryParameters> extends QueriesCommandBase<P> {

    public GetImagesByStorageDomainAndTemplateQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getDiskImageDao()
                .getImagesByStorageIdAndTemplateId(getParameters().getStorageDomainId(),
                        getParameters().getTemplateId()));

    }

}
