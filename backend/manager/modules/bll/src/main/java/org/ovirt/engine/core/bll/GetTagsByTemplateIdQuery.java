package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.GetTagsByTemplateIdParameters;
import org.ovirt.engine.core.dao.TagDao;

public class GetTagsByTemplateIdQuery<P extends GetTagsByTemplateIdParameters> extends
        QueriesCommandBase<P> {

    @Inject
    private TagDao tagDao;

    public GetTagsByTemplateIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(tagDao.getAllForTemplate(getParameters().getTemplateId()));
    }
}
