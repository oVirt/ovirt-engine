package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.LabelDao;

public class GetLabelByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    LabelDao labelDao;

    public GetLabelByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                labelDao.get(getParameters().getId()));
    }
}
