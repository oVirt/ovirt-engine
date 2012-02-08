package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmTemplatesWithPermittedActionQuery<P extends GetEntitiesWithPermittedActionParameters>
        extends QueriesCommandBase<P> {

    public GetVmTemplatesWithPermittedActionQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        P params = getParameters();
        Guid userId = SessionDataContainer.getInstance().getUser(params.getSessionId()).getUserId();
        setReturnValue(DbFacade.getInstance().getVmTemplateDAO().
                getTemplatesWithPermittedAction(userId, params.getActionGroup()));
    }
}
