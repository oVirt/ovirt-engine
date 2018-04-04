package org.ovirt.engine.core.bll.storage.pool;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.CINDERStorageHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ConnectHostToStoragePoolServersParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.StorageDomainDao;

/**
 * Connect host to all Storage server connections in Storage pool. We
 * considering that connection failed only if data domains failed to connect. If
 * Iso/Export domains failed to connect - only log it.
 */
@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class ConnectHostToStoragePoolServersCommand extends
        ConnectHostToStoragePoolServerCommandBase<ConnectHostToStoragePoolServersParameters> {

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private CINDERStorageHelper cinderStorageHelper;
    @Inject
    private StorageDomainDao storageDomainDao;

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

        if (cinderStorageHelper.isActiveCinderDomainAvailable(getStoragePool().getId())) {
            connectSucceeded &= cinderStorageHelper.prepareConnectHostToStoragePoolServers(
                    getContext(), getParameters(), null);
        }

        log.info("Host '{}' storage connection was {} ", getVds().getName(), connectSucceeded ? "succeeded" : "failed");

        return connectSucceeded;
    }

    private boolean connectStorageServersByType(StorageType storageType, List<StorageServerConnections> connections) {
        if (!storageHelperDirector.getItem(storageType).prepareConnectHostToStoragePoolServers(getContext(), getParameters(), connections)) {
            return false;
        }
        if (storageType.isFileDomain()) {
            return connectFileStorageServers(storageType, connections);
        } else {
            return connectStorageServer(storageType, connections, true);
        }
    }

    private boolean connectFileStorageServers(StorageType storageType, List<StorageServerConnections> connections) {
        Map<StorageDomainType, List<StorageServerConnections>> connByType = connections.stream()
                .collect(Collectors.groupingBy(c -> storageDomainDao.getAllByConnectionId(Guid.createGuidFromString(
                        c.getId())).get(0)
                        .getStorageDomainType()));
        boolean connectSucceeded = connectStorageServer(storageType, connByType.get(StorageDomainType.Master), true);
        connectSucceeded &= connectStorageServer(storageType, connByType.get(StorageDomainType.Data), true);

        connectStorageServerIgnoreFailure(storageType, connByType.get(StorageDomainType.ISO), StorageDomainType.ISO);
        connectStorageServerIgnoreFailure(storageType, connByType.get(StorageDomainType.ImportExport), StorageDomainType.ImportExport);

        return connectSucceeded;
    }

    private void connectStorageServerIgnoreFailure(StorageType storageType, List<StorageServerConnections>
            connections, StorageDomainType sdType) {
        try {
            boolean connectSucceeded = connectStorageServer(storageType, connections, false);
            if (!connectSucceeded) {
                log.warn("Ignoring failed connection to domain of type {}.'", sdType);
            }
        } catch (EngineException e) {
            log.warn("Ignoring failed connection to domain of type {}.'", sdType);
        }
    }

    private boolean connectStorageServer(StorageType storageType, List<StorageServerConnections> connections, boolean
            sendNetworkEventOnFailure) {
        if (connections == null || connections.isEmpty()) {
            return true;
        }
        StorageServerConnectionManagementVDSParameters parameters =
                new StorageServerConnectionManagementVDSParameters(getVds().getId(),
                        getStoragePool().getId(), storageType, connections);
        parameters.setSendNetworkEventOnFailure(sendNetworkEventOnFailure);
        Map<String, String> retValues = (Map<String, String>) runVdsCommand(
                VDSCommandType.ConnectStorageServer,
                parameters).getReturnValue();
        return storageHelperDirector.getItem(storageType).isConnectSucceeded(retValues, connections);
    }

}
