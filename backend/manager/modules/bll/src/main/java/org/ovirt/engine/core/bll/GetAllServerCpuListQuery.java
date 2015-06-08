package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetAllServerCpuListParameters;

public class GetAllServerCpuListQuery<P extends GetAllServerCpuListParameters> extends QueriesCommandBase<P> {
    public GetAllServerCpuListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(CpuFlagsManagerHandler.allServerCpuList(getParameters().getVersion()));
    }
}
