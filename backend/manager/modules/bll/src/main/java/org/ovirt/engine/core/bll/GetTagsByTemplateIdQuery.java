package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetTagsByTemplateIdParameters;
import org.ovirt.engine.core.dao.TagDao;

public class GetTagsByTemplateIdQuery<P extends GetTagsByTemplateIdParameters> extends
        QueriesCommandBase<P> {

    @Inject
    private TagDao tagDao;

    public GetTagsByTemplateIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(tagDao.getAllForTemplate(getParameters().getTemplateId()));
    }
}
