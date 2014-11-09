package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetHostArchitectureQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetHostArchitectureQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VDS host = getDbFacade().getVdsDao().get(getParameters().getId());

        ServerCpu sc =
                CpuFlagsManagerHandler.findMaxServerCpuByFlags(host.getCpuFlags(),
                        host.getVdsGroupCompatibilityVersion());

        getQueryReturnValue().setReturnValue(sc == null ? ArchitectureType.undefined : sc.getArchitecture());
    }
}
