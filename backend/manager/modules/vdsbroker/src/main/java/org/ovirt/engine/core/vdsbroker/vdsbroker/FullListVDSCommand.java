package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.FullListVDSCommandParameters;

public class FullListVDSCommand<P extends FullListVDSCommandParameters> extends VdsBrokerCommand<P> {
    private VMListReturn fullVmListReturn;

    public FullListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        fullVmListReturn = getBroker().fullList(getParameters().getVmIds());
        proceedProxyReturnValue();
        Map<String, Object>[] struct = fullVmListReturn.vmList;
        setReturnValue(struct);
    }

    @Override
    protected Status getReturnStatus() {
        return fullVmListReturn.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return fullVmListReturn;
    }

    @Override
    protected boolean getIsPrintReturnValue() {
        return false;
    }
}
