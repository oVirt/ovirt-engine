package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetClustersWithPermittedActionQuery<P extends GetEntitiesWithPermittedActionParameters>
        extends QueriesCommandBase<P> {

    public GetClustersWithPermittedActionQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        P params = getParameters();
        Guid userId =
                ((IVdcUser) SessionDataContainer.getInstance().GetData(params.getSessionId(), "VdcUser")).getUserId();
        setReturnValue(DbFacade.getInstance().getVdsGroupDAO().
                getClustersWithPermittedAction(userId, params.getActionGroup()));
    }
}
