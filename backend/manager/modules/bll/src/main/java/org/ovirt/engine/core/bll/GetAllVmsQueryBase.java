package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.VmDao;

public abstract class GetAllVmsQueryBase<P extends QueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    protected VmDao vmDao;

    @Inject
    private VmHandler vmHandler;

    public GetAllVmsQueryBase(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vmsList = getVMs();
        for (VM vm : vmsList) {
            vmHandler.updateVmGuestAgentVersion(vm);
            vmHandler.updateVmStatistics(vm);
        }
        getQueryReturnValue().setReturnValue(vmsList);
    }

    protected abstract List<VM> getVMs();
}
