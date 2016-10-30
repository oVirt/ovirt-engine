package org.ovirt.engine.core.bll.network.vm;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.network.VnicProfileViewDao;

public class GetAllVnicProfilesQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private VnicProfileViewDao vnicProfileViewDao;

    public GetAllVnicProfilesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(vnicProfileViewDao.getAll(getUserID(), getParameters().isFiltered()));
    }
}
