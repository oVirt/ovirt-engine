package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetCpuProfilesByCpuQosIdQuery extends QueriesCommandBase<IdQueryParameters> {

    public GetCpuProfilesByCpuQosIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getCpuProfileDao()
                .getAllForQos(getParameters().getId()));
    }

}
