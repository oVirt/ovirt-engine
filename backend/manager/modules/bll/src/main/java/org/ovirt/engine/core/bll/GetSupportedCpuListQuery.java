package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetSupportedCpuListParameters;
import org.ovirt.engine.core.compat.Version;

public class GetSupportedCpuListQuery<P extends GetSupportedCpuListParameters> extends QueriesCommandBase<P> {
    public GetSupportedCpuListQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Inject
    protected CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Override
    protected void executeQueryCommand() {
        Version version = getParameters().getVersion() != null ?
                getParameters().getVersion() :
                cpuFlagsManagerHandler.getLatestDictionaryVersion();

        getQueryReturnValue().setReturnValue(cpuFlagsManagerHandler.getSupportedServerCpuList(version, getParameters().getMaxCpuName()));
    }
}
