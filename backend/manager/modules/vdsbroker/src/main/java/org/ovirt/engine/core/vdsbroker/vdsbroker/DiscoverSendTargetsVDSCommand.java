package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.vdscommands.DiscoverSendTargetsVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.storage.StorageConnectionHelper;

public class DiscoverSendTargetsVDSCommand<P extends DiscoverSendTargetsVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    protected IQNListReturnForXmlRpc _result;

    public DiscoverSendTargetsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        _result = getBroker().discoverSendTargets(
                StorageConnectionHelper.getInstance().createStructFromConnection(getParameters().getConnection(), getParameters().getVdsId()));
        proceedProxyReturnValue();
        setReturnValue(_result.isFullTargets() ? parseFullTargets(_result.getIqnList())
                : parseTargets(_result.getIqnList()));
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.getXmlRpcStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }

    private List<StorageServerConnections> parseFullTargets(List<String> iqnList) {
        ArrayList<StorageServerConnections> connections = new ArrayList<>(iqnList.size());
        for (String fullTarget : iqnList) {
            StorageServerConnections con = StorageServerConnections.copyOf(getParameters().getConnection());
            // fullTarget format: <ip>:<port>, <portal> <targetName>
            // e.g 10.35.104.8:3600,1 blue-20G
            String[] tokens = fullTarget.split(",");
            String[] address = tokens[0].split(":");
            String[] literals = tokens[1].split(" ");

            con.setConnection(address[0]);
            con.setPort(address[1]);
            con.setPortal(literals[0]);
            con.setIqn(literals[1]);
            connections.add(con);
        }

        return connections;
    }

    private List<StorageServerConnections> parseTargets(List<String> iqnList) {
        List<StorageServerConnections> connections = new ArrayList<>(iqnList.size());
        for (String iqn : iqnList) {
            StorageServerConnections con = StorageServerConnections.copyOf(getParameters().getConnection());
            con.setIqn(iqn);
            connections.add(con);
        }
        return connections;
    }

}
