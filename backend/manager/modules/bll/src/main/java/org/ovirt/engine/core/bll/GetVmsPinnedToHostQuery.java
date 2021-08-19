package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDao;

public class GetVmsPinnedToHostQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmDao vmDao;

    @Inject
    private VmHandler vmHandler;

    public GetVmsPinnedToHostQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override protected void executeQueryCommand() {
        List<VM> vms = vmDao.getAllPinnedToHost(getParameters().getId());
        if (vms != null) {
            vms.forEach(vmHandler::updateVmStatistics);
            vms.forEach(vmHandler::updateConfiguredCpuVerb);
        }

        setReturnValue(vms);
    }

}
