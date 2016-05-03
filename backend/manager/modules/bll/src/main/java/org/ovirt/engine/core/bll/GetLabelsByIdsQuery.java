package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.dao.LabelDao;

public class GetLabelsByIdsQuery<P extends IdsQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    LabelDao labelDao;

    public GetLabelsByIdsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                labelDao.getAllByIds(getParameters().getIds()));
    }
}
