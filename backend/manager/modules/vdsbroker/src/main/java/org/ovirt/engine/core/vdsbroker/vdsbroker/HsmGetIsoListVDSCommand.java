package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.HSMGetIsoListParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.FileStatsReturnForXmlRpc;

public class HsmGetIsoListVDSCommand<P extends HSMGetIsoListParameters> extends VdsBrokerCommand<P> {
    private FileStatsReturnForXmlRpc isoList;

    public HsmGetIsoListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        isoList = getBroker().getIsoList(getParameters().getStoragePoolId().toString());
        proceedProxyReturnValue();
        setReturnValue(isoList.getFileStats());
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return isoList;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return isoList.getXmlRpcStatus();
    }
}
