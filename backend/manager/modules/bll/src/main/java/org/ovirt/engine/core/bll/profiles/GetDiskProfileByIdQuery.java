package org.ovirt.engine.core.bll.profiles;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetDiskProfileByIdQuery extends QueriesCommandBase<IdQueryParameters> {

    public GetDiskProfileByIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getDiskProfileDao().get(getParameters().getId()));
    }

}
