package org.ovirt.engine.core.bll.scheduling.queries;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetAffinityGroupByIdQuery extends QueriesCommandBase<IdQueryParameters> {

    public GetAffinityGroupByIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getAffinityGroupDao().get(getParameters().getId()));
    }

}
