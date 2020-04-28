package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.UUIDListReturn;

public class ListVmCheckpointsVDSCommand<P extends VdsAndVmIDVDSParametersBase> extends VdsBrokerCommand<P> {
    private UUIDListReturn uuidListReturn;

    public ListVmCheckpointsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        uuidListReturn = getBroker().listVmCheckpoints(getParameters().getVmId().toString());
        proceedProxyReturnValue();

        setReturnValue(Stream.of(uuidListReturn.getUUIDList()).map(Guid::new).collect(Collectors.toList()));
    }

    @Override
    protected Status getReturnStatus() {
        return uuidListReturn.getStatus();
    }
}
