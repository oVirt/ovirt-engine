package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;

public class GetVmTemplatesWithPermittedActionQuery<P extends GetEntitiesWithPermittedActionParameters>
        extends QueriesCommandBase<P> {

    public GetVmTemplatesWithPermittedActionQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        P params = getParameters();
        setReturnValue(getDbFacade().getVmTemplateDao().
                getTemplatesWithPermittedAction(getUserID(), params.getActionGroup()));
    }
}
