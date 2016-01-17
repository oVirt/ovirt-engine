package org.ovirt.engine.core.bll.storage.pool;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.CINDERStorageHelper;
import org.ovirt.engine.core.bll.storage.connection.StorageHelperDirector;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ConnectHostToStoragePoolServersParameters;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

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

    @Override
    protected void executeCommand() {
        initConnectionList(getParameters().isConnectToInactiveDomains());
        setSucceeded(connectStorageServer(getConnectionsTypeMap()));

        if (!getSucceeded()) {
            auditLogDirector.log(this, AuditLogType.CONNECT_STORAGE_SERVERS_FAILED);
        }
    }

    private boolean connectStorageServer(Map<StorageType, List<StorageServerConnections>> connectionsByType) {
        boolean connectSucceeded = true;

        for (Map.Entry<StorageType, List<StorageServerConnections>> connectionToType : connectionsByType.entrySet()) {
            StorageType connectionsType = connectionToType.getKey();
            List<StorageServerConnections> connections = connectionToType.getValue();
            connectSucceeded = connectStorageServersByType(connectionsType, connections) && connectSucceeded;
        }

        CINDERStorageHelper CINDERStorageHelper = new CINDERStorageHelper();
        if (CINDERStorageHelper.isActiveCinderDomainAvailable(getStoragePool().getId())) {
            connectSucceeded &= CINDERStorageHelper.prepareConnectHostToStoragePoolServers(
                    getContext(), getParameters(), null);
        }

        log.info("Host '{}' storage connection was {} ", getVds().getName(), connectSucceeded ? "succeeded" : "failed");

        return connectSucceeded;
    }

    private boolean connectStorageServersByType(StorageType storageType, List<StorageServerConnections> connections) {
        if (!StorageHelperDirector.getInstance().getItem(storageType).prepareConnectHostToStoragePoolServers(getContext(), getParameters(), connections)) {
            return false;
        }

        Map<String, String> retValues = (Map<String, String>) runVdsCommand(
                        VDSCommandType.ConnectStorageServer,
                        new StorageServerConnectionManagementVDSParameters(getVds().getId(),
                                getStoragePool().getId(), storageType, connections)).getReturnValue();
        return StorageHelperDirector.getInstance().getItem(storageType).isConnectSucceeded(retValues, connections);
    }

}
