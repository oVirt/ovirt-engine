package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.vdscommands.GetStorageConnectionsListVDSCommandParameters;


public class GetStorageConnectionsListVDSCommand<P extends GetStorageConnectionsListVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    private ServerConnectionListReturnForXmlRpc _result;

    public GetStorageConnectionsListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        _result = getBroker().getStorageConnectionsList(
                getParameters().getStoragePoolId().toString());
        proceedProxyReturnValue();

        // parse result to storage_server_connections
        ArrayList<StorageServerConnections> connections = new ArrayList<StorageServerConnections>();
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
