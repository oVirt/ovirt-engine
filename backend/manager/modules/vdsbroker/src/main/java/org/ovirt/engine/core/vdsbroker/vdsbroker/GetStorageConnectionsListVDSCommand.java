package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.vdscommands.GetStorageConnectionsListVDSCommandParameters;


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
        proceedProxyReturnValue();

        // parse result to storage_server_connections
        java.util.ArrayList<StorageServerConnections> connections =
                new java.util.ArrayList<StorageServerConnections>();
        for (Map<String, Object> x : _result.mConnectionList) {
            StorageServerConnections storageCon = new StorageServerConnections();
            if (x.containsKey("serverType")) {
                storageCon.setstorage_type((StorageType) x.get("serverType"));
            }
            if (x.containsKey("target")) {
                storageCon.setconnection(x.get("target").toString());
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
