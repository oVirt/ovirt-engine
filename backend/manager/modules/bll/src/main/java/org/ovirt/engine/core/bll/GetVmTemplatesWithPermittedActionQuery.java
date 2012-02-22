package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmTemplatesWithPermittedActionQuery<P extends GetEntitiesWithPermittedActionParameters>
        extends QueriesCommandBase<P> {

    public GetVmTemplatesWithPermittedActionQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        P params = getParameters();
        setReturnValue(DbFacade.getInstance().getVmTemplateDAO().
                getTemplatesWithPermittedAction(getUserID(), params.getActionGroup()));
    }
}
