package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetVmIconDefaultParameters;
import org.ovirt.engine.core.dao.VmIconDefaultDao;

public class GetVmIconDefaultQuery extends QueriesCommandBase<GetVmIconDefaultParameters> {

    @Inject
    private VmIconDefaultDao vmIconDefaultDao;

    public GetVmIconDefaultQuery(GetVmIconDefaultParameters parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(vmIconDefaultDao.getByOperatingSystemId(getParameters().getOperatingSystemId()));
    }
}
