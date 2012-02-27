package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.List;

import org.ovirt.engine.core.common.vdscommands.FullListVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class FullListVdsCommand<P extends FullListVDSCommandParameters> extends VdsBrokerCommand<P> {
    private VMListReturnForXmlRpc fullVmListReturn;

    public FullListVdsCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        List<String> vmIds = getParameters().getVmIds();
        String[] vmIdsArray = vmIds.toArray(new String[vmIds.size()]);
        fullVmListReturn = getBroker().list(Boolean.TRUE.toString(), vmIdsArray);
        ProceedProxyReturnValue();
        XmlRpcStruct[] struct = fullVmListReturn.mVmList;
        setReturnValue(struct);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return fullVmListReturn.mStatus;
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
