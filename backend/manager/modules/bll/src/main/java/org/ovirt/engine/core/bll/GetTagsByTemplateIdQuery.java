package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetTagsByTemplateIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetTagsByTemplateIdQuery<P extends GetTagsByTemplateIdParameters> extends
        QueriesCommandBase<P> {
    public GetTagsByTemplateIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getTagDao()
                        .getAllForTemplate(getParameters().getTemplateId()));
    }
}
