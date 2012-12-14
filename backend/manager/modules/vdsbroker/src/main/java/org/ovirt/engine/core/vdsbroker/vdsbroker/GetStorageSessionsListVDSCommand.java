package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class GetStorageSessionsListVDSCommand<P extends VdsIdVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    protected SessionsListReturnForXmlRpc _result;

    public GetStorageSessionsListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().getSessionList();
        ProceedProxyReturnValue();
        setReturnValue(ParseSessionsList(_result.sessionsList));
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    private java.util.ArrayList<StorageServerConnections> ParseSessionsList(XmlRpcStruct[] sessionsList) {
        java.util.ArrayList<StorageServerConnections> result = new java.util.ArrayList<StorageServerConnections>(
                sessionsList.length);
        for (XmlRpcStruct session : sessionsList) {
            result.add(GetDeviceListVDSCommand.ParseConnection(session));
        }
        return result;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
