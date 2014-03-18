package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetNumberOfVmsInVdsGroupByVdsGroupIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetNumberOfVmsInVdsGroupByVdsGroupIdQuery(P parameters) {
            super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        int numOfVms = getDbFacade().getVdsGroupDao().getVmsCountByClusterId(getParameters().getId());
        getQueryReturnValue().setReturnValue(numOfVms);
    }
}
