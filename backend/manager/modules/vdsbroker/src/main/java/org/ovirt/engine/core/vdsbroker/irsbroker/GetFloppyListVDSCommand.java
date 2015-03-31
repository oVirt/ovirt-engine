package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSExceptionBase;

public class GetFloppyListVDSCommand<P extends IrsBaseVDSCommandParameters> extends GetIsoListVDSCommand<P> {
    protected FileStatsReturnForXmlRpc floppyList;

    public GetFloppyListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        floppyList = getIrsProxy().getFloppyList(getParameters().getStoragePoolId().toString());
        proceedProxyReturnValue();
        setReturnValue(floppyList.getFileStats());
    }

    @Override
    protected VDSExceptionBase createDefaultConcreteException(String errorMessage) {
        return new IrsOperationFailedNoFailoverException(errorMessage);
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return floppyList;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return floppyList.getXmlRpcStatus();
    }
}
