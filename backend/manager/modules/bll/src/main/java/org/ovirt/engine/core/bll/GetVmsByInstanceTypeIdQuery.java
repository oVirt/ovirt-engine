package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDao;

/**
 * A query to retrieve all the VMs connected to a given instance type.
 */
public class GetVmsByInstanceTypeIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDao vmDao;

    public GetVmsByInstanceTypeIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(vmDao.getVmsListByInstanceType(getParameters().getId()));
    }
}
