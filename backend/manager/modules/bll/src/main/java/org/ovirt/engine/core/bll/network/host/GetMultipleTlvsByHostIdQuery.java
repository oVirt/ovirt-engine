package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.vdscommands.GetLldpVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;

public class GetMultipleTlvsByHostIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetMultipleTlvsByHostIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        String[] names = new String[0];
        GetLldpVDSCommandParameters lldpVDSCommandParameters =
                new GetLldpVDSCommandParameters(getParameters().getId(), names);

        VDSReturnValue command = runVdsCommand(VDSCommandType.GetLldp, lldpVDSCommandParameters);
        getQueryReturnValue().setReturnValue(command.getReturnValue());
    }
}
