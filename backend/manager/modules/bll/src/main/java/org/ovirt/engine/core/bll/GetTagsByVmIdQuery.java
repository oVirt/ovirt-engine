package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetTagsByVmIdParameters;
import org.ovirt.engine.core.dao.TagDao;

public class GetTagsByVmIdQuery<P extends GetTagsByVmIdParameters> extends
        QueriesCommandBase<P> {

    @Inject
    private TagDao tagDao;

    public GetTagsByVmIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(tagDao.getAllForVm(getParameters().getVmId()));
    }
}
