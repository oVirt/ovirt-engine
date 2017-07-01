package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class GetVmTemplateCountQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private VmTemplateDao vmTemplateDao;

    public GetVmTemplateCountQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(vmTemplateDao.getCount());
    }
}
