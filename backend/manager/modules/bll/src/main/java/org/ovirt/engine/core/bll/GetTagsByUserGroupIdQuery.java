package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetTagsByUserGroupIdParameters;
import org.ovirt.engine.core.dao.TagDao;

public class GetTagsByUserGroupIdQuery<P extends GetTagsByUserGroupIdParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private TagDao tagDao;

    public GetTagsByUserGroupIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(tagDao.getAllForUserGroups(getParameters().getGroupId()));
    }
}
