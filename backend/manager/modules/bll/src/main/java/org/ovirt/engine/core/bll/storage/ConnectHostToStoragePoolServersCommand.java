package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.context.CommandContext;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ConnectHostToStoragePoolServersParameters;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

/**
 * Connect host to all Storage server connections in Storage pool. We
 * considering that connection failed only if data domains failed to connect. If
 * Iso/Export domains failed to connect - only log it.
 */
@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class ConnectHostToStoragePoolServersCommand extends
        ConnectHostToStoragePoolServerCommandBase<ConnectHostToStoragePoolServersParameters> {

    public ConnectHostToStoragePoolServersCommand(ConnectHostToStoragePoolServersParameters parameters,
            CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePool(parameters.getStoragePool());
        setVds(parameters.getVds());
    }

    public ConnectHostToStoragePoolServersCommand(ConnectHostToStoragePoolServersParameters parameters) {
        super(parameters);
        setStoragePool(parameters.getStoragePool());
        setVds(parameters.getVds());
    }

    @Override
    protected void executeCommand() {
        initConnectionList(getParameters().isConnectToInactiveDomains());
        setSucceeded(connectStorageServer(getConnectionsTypeMap()));

        if (!getSucceeded()) {
           AuditLogDirector.log(this, AuditLogType.CONNECT_STORAGE_SERVERS_FAILED);
        }
    }

    private boolean connectStorageServer(Map<StorageType, List<StorageServerConnections>> connectionsByType) {
        boolean connectSucceeded = true;

        for (Map.Entry<StorageType, List<StorageServerConnections>> connectionToType : connectionsByType.entrySet()) {
            StorageType connectionsType = connectionToType.getKey();
            List<StorageServerConnections> connections = connectionToType.getValue();
            connectSucceeded = connectStorageServersByType(connectionsType, connections) && connectSucceeded;
        }

        log.infoFormat("Host {0} storage connection was {1} ", getVds().getName(), connectSucceeded ? "succeeded" : "failed");

        return connectSucceeded;
    }

    private boolean connectStorageServersByType(StorageType storageType, List<StorageServerConnections> connections) {
        if (storageType == StorageType.ISCSI) {
            connections = ISCSIStorageHelper.updateIfaces(connections, getVds().getId());
        }

        Map<String, String> retValues = (Map<String, String>) runVdsCommand(
                        VDSCommandType.ConnectStorageServer,
                        new StorageServerConnectionManagementVDSParameters(getVds().getId(),
                                getStoragePool().getId(), storageType, connections)).getReturnValue();
        return StorageHelperDirector.getInstance().getItem(storageType).isConnectSucceeded(retValues, connections);
    }
}
