package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetClustersWithPermittedActionQuery<P extends GetEntitiesWithPermittedActionParameters>
        extends QueriesCommandBase<P> {

    public GetClustersWithPermittedActionQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        P params = getParameters();
        setReturnValue(DbFacade.getInstance().getVdsGroupDAO().
                getClustersWithPermittedAction(getUserID(), params.getActionGroup()));
    }
}
