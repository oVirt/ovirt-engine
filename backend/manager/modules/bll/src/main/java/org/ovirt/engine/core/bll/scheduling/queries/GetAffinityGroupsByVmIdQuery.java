package org.ovirt.engine.core.bll.scheduling.queries;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetAffinityGroupsByVmIdQuery extends QueriesCommandBase<IdQueryParameters> {

    public GetAffinityGroupsByVmIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getAffinityGroupDao().getAllAffinityGroupsByVmId(getParameters().getId()));
    }

}
