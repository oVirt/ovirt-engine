package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.queries.GetCpuByFlagsParameters;

public class GetCpuByFlagsQuery<P extends GetCpuByFlagsParameters> extends QueriesCommandBase<P> {

    public GetCpuByFlagsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Inject
    protected CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Override
    protected void executeQueryCommand() {
        ServerCpu cpu = cpuFlagsManagerHandler.findMaxServerCpuByFlags(
                getParameters().getFlags(),
                getParameters().getNewVersion());
        getQueryReturnValue().setReturnValue(cpu);
    }
}
