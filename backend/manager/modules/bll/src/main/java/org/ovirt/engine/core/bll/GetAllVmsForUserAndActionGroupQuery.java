package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.dao.VmDao;

public class GetAllVmsForUserAndActionGroupQuery<P extends GetEntitiesWithPermittedActionParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmDao vmDao;

    public GetAllVmsForUserAndActionGroupQuery(P parameters) {
        super(parameters);
    }

    public GetAllVmsForUserAndActionGroupQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vmsList = vmDao.getAllForUserAndActionGroup(getUserID(), getParameters().getActionGroup());
        for (VM vm : vmsList) {
            VmHandler.updateVmGuestAgentVersion(vm);
        }
        getQueryReturnValue().setReturnValue(vmsList);
    }
}
