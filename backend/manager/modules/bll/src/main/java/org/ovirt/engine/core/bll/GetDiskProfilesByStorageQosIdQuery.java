package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetDiskProfilesByStorageQosIdQuery extends QueriesCommandBase<IdQueryParameters> {

    public GetDiskProfilesByStorageQosIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getDiskProfileDao()
                .getAllForQos(getParameters().getId()));
    }

}
