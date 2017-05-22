package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetTagsByUserIdParameters;
import org.ovirt.engine.core.dao.TagDao;

public class GetTagsByUserIdQuery<P extends GetTagsByUserIdParameters> extends
        QueriesCommandBase<P> {

    @Inject
    private TagDao tagDao;

    public GetTagsByUserIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(tagDao.getAllForUsers(getParameters().getUserId()));
    }
}
