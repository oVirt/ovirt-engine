package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.VmIconDao;

import javax.inject.Inject;

public class GetAllVmIconsQuery extends QueriesCommandBase<VdcQueryParametersBase> {

    @Inject
    private VmIconDao vmIconDao;

    public GetAllVmIconsQuery(VdcQueryParametersBase parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(vmIconDao.getAll());
    }
}
