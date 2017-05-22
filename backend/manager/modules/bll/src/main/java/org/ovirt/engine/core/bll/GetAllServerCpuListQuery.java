package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetAllServerCpuListParameters;

public class GetAllServerCpuListQuery<P extends GetAllServerCpuListParameters> extends QueriesCommandBase<P> {
    public GetAllServerCpuListQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Inject
    protected CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(cpuFlagsManagerHandler.allServerCpuList(getParameters().getVersion()));
    }
}
