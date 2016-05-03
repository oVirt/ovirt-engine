package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.LabelDao;

public class GetAllLabelsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    LabelDao labelDao;

    public GetAllLabelsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(labelDao.getAll());
    }
}
