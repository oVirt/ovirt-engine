package org.ovirt.engine.core.bll.network.vm;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.network.VnicProfileViewDao;

public class GetAllVnicProfilesQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private VnicProfileViewDao vnicProfileViewDao;

    public GetAllVnicProfilesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(vnicProfileViewDao.getAll(getUserID(), getParameters().isFiltered()));
    }
}
