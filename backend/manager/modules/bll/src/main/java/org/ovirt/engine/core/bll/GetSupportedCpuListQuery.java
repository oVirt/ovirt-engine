package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetSupportedCpuListParameters;

public class GetSupportedCpuListQuery<P extends GetSupportedCpuListParameters> extends QueriesCommandBase<P> {
    public GetSupportedCpuListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getCpuFlagsManagerHandler().getSupportedServerCpuList(getCpuFlagsManagerHandler().getLatestDictionaryVersion(), getParameters().getMaxCpuName()));
    }
}
