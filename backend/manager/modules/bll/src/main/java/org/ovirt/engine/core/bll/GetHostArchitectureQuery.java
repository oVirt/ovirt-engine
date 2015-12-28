package org.ovirt.engine.core.bll;

import javax.inject.Inject;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetHostArchitectureQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetHostArchitectureQuery(P parameters) {
        super(parameters);
    }

    @Inject
    protected CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Override
    protected void executeQueryCommand() {
        VDS host = getDbFacade().getVdsDao().get(getParameters().getId());

        ServerCpu sc =
                cpuFlagsManagerHandler.findMaxServerCpuByFlags(host.getCpuFlags(),
                        host.getClusterCompatibilityVersion());

        getQueryReturnValue().setReturnValue(sc == null ? ArchitectureType.undefined : sc.getArchitecture());
    }
}
