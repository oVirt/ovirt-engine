package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.LabelDao;

public class GetLabelByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    LabelDao labelDao;

    public GetLabelByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                labelDao.get(getParameters().getId()));
    }
}
