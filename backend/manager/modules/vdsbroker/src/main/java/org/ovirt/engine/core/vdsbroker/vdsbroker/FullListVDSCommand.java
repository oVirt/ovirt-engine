package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.FullListVDSCommandParameters;

public class FullListVDSCommand<P extends FullListVDSCommandParameters> extends VdsBrokerCommand<P> {
    private VMListReturnForXmlRpc fullVmListReturn;

    public FullListVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void executeVdsBrokerCommand() {
        List<String> vmIds = getParameters().getVmIds();
        String[] vmIdsArray = vmIds.toArray(new String[vmIds.size()]);
        fullVmListReturn = getBroker().list(Boolean.TRUE.toString(), vmIdsArray);
        proceedProxyReturnValue();
        Map<String, Object>[] struct = fullVmListReturn.vmList;
        setReturnValue(struct);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
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
