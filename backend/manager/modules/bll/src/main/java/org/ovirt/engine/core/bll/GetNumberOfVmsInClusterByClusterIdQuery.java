package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetNumberOfVmsInClusterByClusterIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetNumberOfVmsInClusterByClusterIdQuery(P parameters) {
            super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        int numOfVms = getDbFacade().getClusterDao().getVmsCountByClusterId(getParameters().getId());
        getQueryReturnValue().setReturnValue(numOfVms);
    }
}
