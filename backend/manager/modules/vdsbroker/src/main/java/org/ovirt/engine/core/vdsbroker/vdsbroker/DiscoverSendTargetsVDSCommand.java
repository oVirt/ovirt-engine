package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.vdscommands.DiscoverSendTargetsVDSCommandParameters;

public class DiscoverSendTargetsVDSCommand<P extends DiscoverSendTargetsVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    protected IQNListReturnForXmlRpc _result;

    public DiscoverSendTargetsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().discoverSendTargets(
                ConnectStorageServerVDSCommand.CreateStructFromConnection(getParameters().getConnection(),null));
        ProceedProxyReturnValue();
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

    private List<storage_server_connections> parseFullTargets(List<String> iqnList) {
        ArrayList<storage_server_connections> connections = new ArrayList<storage_server_connections>(iqnList.size());
        for (String fullTarget : iqnList) {
            storage_server_connections con = storage_server_connections.copyOf(getParameters().getConnection());
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

    private List<storage_server_connections> parseTargets(List<String> iqnList) {
        List<storage_server_connections> connections = new ArrayList<storage_server_connections>(iqnList.size());
        for (String iqn : iqnList) {
            storage_server_connections con = storage_server_connections.copyOf(getParameters().getConnection());
            con.setiqn(iqn);
            connections.add(con);
        }
        return connections;
    }

}
