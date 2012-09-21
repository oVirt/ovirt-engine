package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetHostsByClusterIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetHostsByClusterIdQuery<P extends GetHostsByClusterIdParameters> extends QueriesCommandBase<P> {
    public GetHostsByClusterIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                        .getVdsDao()
                        .getAllForVdsGroup(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }

}
