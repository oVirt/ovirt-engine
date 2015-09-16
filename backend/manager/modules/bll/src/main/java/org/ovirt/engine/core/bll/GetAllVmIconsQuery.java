package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.VmIconDao;

public class GetAllVmIconsQuery extends QueriesCommandBase<VdcQueryParametersBase> {

    @Inject
    private VmIconDao vmIconDao;

    public GetAllVmIconsQuery(VdcQueryParametersBase parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(vmIconDao.getAll(getUserID(), getParameters().isFiltered()));
    }
}
