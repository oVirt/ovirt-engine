package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetVmFromOvaQueryParameters;
import org.ovirt.engine.core.common.vdscommands.GetOvaInfoParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public class GetVmFromOvaQuery<T extends GetVmFromOvaQueryParameters> extends QueriesCommandBase<T> {

    public GetVmFromOvaQuery(T parameters) {
        this(parameters, null);
    }

    public GetVmFromOvaQuery(T parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(getVmInfoFromOvaFile());
    }

    private Object getVmInfoFromOvaFile() {
        return runVdsCommand(VDSCommandType.GetOvaInfo, buildGetOvaInfoParameters()).getReturnValue();
    }

    private GetOvaInfoParameters buildGetOvaInfoParameters() {
        return new GetOvaInfoParameters(
                getParameters().getVdsId(),
                getParameters().getPath());
    }
}
