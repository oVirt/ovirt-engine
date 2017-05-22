package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetTagsByVdsIdParameters;
import org.ovirt.engine.core.dao.TagDao;

public class GetTagsByVdsIdQuery<P extends GetTagsByVdsIdParameters> extends
        QueriesCommandBase<P> {

    @Inject
    private TagDao tagDao;

    public GetTagsByVdsIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(tagDao.getAllForVds(getParameters().getVdsId()));
    }
}
