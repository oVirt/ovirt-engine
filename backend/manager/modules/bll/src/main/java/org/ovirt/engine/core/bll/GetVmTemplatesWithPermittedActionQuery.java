package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class GetVmTemplatesWithPermittedActionQuery<P extends GetEntitiesWithPermittedActionParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private VmTemplateDao vmTemplateDao;

    public GetVmTemplatesWithPermittedActionQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        P params = getParameters();
        setReturnValue(vmTemplateDao.getTemplatesWithPermittedAction(getUserID(), params.getActionGroup()));
    }
}
