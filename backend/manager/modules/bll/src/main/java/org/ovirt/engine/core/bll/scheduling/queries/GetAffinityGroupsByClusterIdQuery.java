package org.ovirt.engine.core.bll.scheduling.queries;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetAffinityGroupsByClusterIdQuery extends QueriesCommandBase<IdQueryParameters> {

    public GetAffinityGroupsByClusterIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getAffinityGroupDao().getAllAffinityGroupsByClusterId(getParameters().getId()));
    }

}
