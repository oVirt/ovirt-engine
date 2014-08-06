package org.ovirt.engine.core.bll.profiles;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetCpuProfileByIdQuery extends QueriesCommandBase<IdQueryParameters> {

    public GetCpuProfileByIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getCpuProfileDao().get(getParameters().getId()));
    }

}
