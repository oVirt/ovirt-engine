package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class GetStorageConnectionsListVDSCommand<P extends GetStorageConnectionsListVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    private ServerConnectionListReturnForXmlRpc _result;

    public GetStorageConnectionsListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().getStorageConnectionsList(
                getParameters().getStoragePoolId().toString());
        ProceedProxyReturnValue();

        // parse result to storage_server_connections
        java.util.ArrayList<storage_server_connections> connections =
                new java.util.ArrayList<storage_server_connections>();
        for (XmlRpcStruct x : _result.mConnectionList) {
            storage_server_connections storageCon = new storage_server_connections();
            if (x.contains("serverType")) {
                storageCon.setstorage_type((StorageType) x.getItem("serverType"));
            }
            if (x.contains("target")) {
                storageCon.setconnection(x.getItem("target").toString());
            }
            connections.add(storageCon);
        }
        setReturnValue(connections);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
