package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.VmIconDao;

public class GetAllVmIconsQuery extends QueriesCommandBase<VdcQueryParametersBase> {

    @Inject
    private VmIconDao vmIconDao;

    public GetAllVmIconsQuery(VdcQueryParametersBase parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(vmIconDao.getAll(getUserID(), getParameters().isFiltered()));
    }
}
