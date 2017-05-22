package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VdsDao;

public class GetHostArchitectureQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetHostArchitectureQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Inject
    protected CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Inject
    private VdsDao vdsDao;

    @Override
    protected void executeQueryCommand() {
        VDS host = vdsDao.get(getParameters().getId());

        ServerCpu sc =
                cpuFlagsManagerHandler.findMaxServerCpuByFlags(host.getCpuFlags(),
                        host.getClusterCompatibilityVersion());

        getQueryReturnValue().setReturnValue(sc == null ? ArchitectureType.undefined : sc.getArchitecture());
    }
}
