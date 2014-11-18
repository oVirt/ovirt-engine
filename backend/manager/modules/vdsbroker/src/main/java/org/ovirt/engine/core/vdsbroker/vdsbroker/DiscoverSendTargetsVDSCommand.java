package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.vdscommands.DiscoverSendTargetsVDSCommandParameters;

public class DiscoverSendTargetsVDSCommand<P extends DiscoverSendTargetsVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    protected IQNListReturnForXmlRpc _result;

    public DiscoverSendTargetsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        _result = getBroker().discoverSendTargets(
                ConnectStorageServerVDSCommand.createStructFromConnection(getParameters().getConnection(), null));
        proceedProxyReturnValue();
        setReturnValue(_result.isFullTargets() ? parseFullTargets(_result.getIqnList())
                : parseTargets(_result.getIqnList()));
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }

    private List<StorageServerConnections> parseFullTargets(List<String> iqnList) {
        ArrayList<StorageServerConnections> connections = new ArrayList<StorageServerConnections>(iqnList.size());
        for (String fullTarget : iqnList) {
            StorageServerConnections con = StorageServerConnections.copyOf(getParameters().getConnection());
            // fullTarget format: <ip>:<port>, <portal> <targetName>
            // e.g 10.35.104.8:3600,1 blue-20G
            String[] tokens = fullTarget.split(",");
            String[] address = tokens[0].split(":");
            String[] literals = tokens[1].split(" ");

            con.setconnection(address[0]);
            con.setport(address[1]);
            con.setportal(literals[0]);
            con.setiqn(literals[1]);
            connections.add(con);
        }

        return connections;
    }

    private List<StorageServerConnections> parseTargets(List<String> iqnList) {
        List<StorageServerConnections> connections = new ArrayList<StorageServerConnections>(iqnList.size());
        for (String iqn : iqnList) {
            StorageServerConnections con = StorageServerConnections.copyOf(getParameters().getConnection());
            con.setiqn(iqn);
            connections.add(con);
        }
        return connections;
    }

}
