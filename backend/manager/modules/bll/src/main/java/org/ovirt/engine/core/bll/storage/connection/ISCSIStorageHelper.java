package org.ovirt.engine.core.bll.storage.connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ConnectHostToStoragePoolServersParameters;
import org.ovirt.engine.core.common.action.HostStoragePoolParametersBase;
import org.ovirt.engine.core.common.action.SyncLunsInfoForBlockStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ISCSIStorageHelper extends StorageHelperBase {
    private static final Logger log = LoggerFactory.getLogger(ISCSIStorageHelper.class);

    @Inject
    private StorageServerConnectionLunMapDao storageServerConnectionLunMapDao;
    @Inject
    private InterfaceDao interfaceDao;

    @Override
    public Collection<StorageType> getTypes() {
        return Collections.singleton(StorageType.ISCSI);
    }

    @Override
    protected Pair<Boolean, EngineFault> runConnectionStorageToDomain(StorageDomain storageDomain, Guid vdsId, int type) {
        return runConnectionStorageToDomain(storageDomain, vdsId, type, null, Guid.Empty);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Pair<Boolean, EngineFault> runConnectionStorageToDomain(StorageDomain storageDomain,
            Guid vdsId,
            int type,
            LUNs lun,
            Guid storagePoolId) {
        boolean isSuccess = true;
        VDSReturnValue returnValue = null;
        List<StorageServerConnections> list =
                (lun == null) ? storageServerConnectionDao.getAllForVolumeGroup(storageDomain.getStorage())
                        : lun.getLunConnections();

        if (list.size() != 0) {
            if (VDSCommandType.forValue(type) == VDSCommandType.DisconnectStorageServer) {
                list = filterConnectionsUsedByOthers(list, storageDomain.getStorage(), lun != null ? lun.getLUNId()
                        : "");
            } else if (VDSCommandType.forValue(type) == VDSCommandType.ConnectStorageServer) {
                list = updateIfaces(list, vdsId);
            }
            Guid poolId = storagePoolId;
            if (storageDomain != null && storageDomain.getStoragePoolId() != null) {
                poolId = storageDomain.getStoragePoolId();
            }
            returnValue = resourceManager
                    .runVdsCommand(
                            VDSCommandType.forValue(type),
                            new StorageServerConnectionManagementVDSParameters(vdsId,
                                    poolId, StorageType.ISCSI, list));
            isSuccess = returnValue.getSucceeded();
            if (isSuccess && VDSCommandType.forValue(type) == VDSCommandType.ConnectStorageServer) {
                isSuccess = isConnectSucceeded((Map<String, String>) returnValue.getReturnValue(), list);
            }
        }
        EngineFault engineFault = null;
        if (!isSuccess && returnValue.getVdsError() != null) {
            engineFault = new EngineFault();
            engineFault.setError(returnValue.getVdsError().getCode());
        }
        return new Pair<>(isSuccess, engineFault);
    }

    public List<StorageServerConnections> updateIfaces(List<StorageServerConnections> conns, Guid vdsId) {
        List<StorageServerConnections> res = new ArrayList<>(conns);

        for (StorageServerConnections conn : conns) {
            // Get list of endpoints (nics or vlans) that will initiate iscsi sessions.
            // Targets are represented by StorageServerConnections object (connection, iqn, port, portal).
            List<VdsNetworkInterface> ifaces =
                    interfaceDao.getIscsiIfacesByHostIdAndStorageTargetId(vdsId, conn.getId());

            if (!ifaces.isEmpty()) {
                VdsNetworkInterface removedInterface = ifaces.remove(0);
                setInterfaceProperties(conn, removedInterface);

                // Iscsi target is represented by connection object, therefore if this target is approachable
                // from more than one endpoint(initiator) we have to clone this connection per endpoint.
                for (VdsNetworkInterface iface : ifaces) {
                    StorageServerConnections newConn = StorageServerConnections.copyOf(conn);
                    newConn.setId(Guid.newGuid().toString());
                    setInterfaceProperties(newConn, iface);
                    res.add(newConn);
                }
            }
        }

        return res;
    }

    @Override
    public boolean prepareConnectHostToStoragePoolServers(CommandContext cmdContext,
            ConnectHostToStoragePoolServersParameters parameters,
            List<StorageServerConnections> connections) {
        return prepareStorageServer(parameters, connections);
    }

    @Override
    public void prepareDisconnectHostFromStoragePoolServers(HostStoragePoolParametersBase parameters,
            List<StorageServerConnections> connections) {
        prepareStorageServer(parameters, connections);
    }

    private boolean prepareStorageServer(HostStoragePoolParametersBase parameters,
            List<StorageServerConnections> connections) {
        List<StorageServerConnections> res = updateIfaces(connections, parameters.getVds().getId());
        connections.clear();
        connections.addAll(res);
        return true;
    }

    private static void setInterfaceProperties(StorageServerConnections conn, VdsNetworkInterface iface) {
        conn.setIface(iface.getName());
        conn.setNetIfaceName(iface.isBridged() ? iface.getNetworkName() : iface.getName());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<StorageServerConnections> filterConnectionsUsedByOthers(
            List<StorageServerConnections> connections, String vgId, final String lunId) {
        // if we have lun id then filter by this lun
        // else get vg's luns from db
        List<String> lunsByVgWithNoDisks = new ArrayList<>();
        if (lunId.isEmpty()) {
            List<String> lunsByVg =
                    lunDao.getAllForVolumeGroup(vgId).stream().map(LUNs::getLUNId).collect(Collectors.toList());

            // if a luns were retrieved by vgId, they can belongs not only to storage but also to disks
            // at that case they should left at db
            for (String lunIdByVg : lunsByVg) {
                if (diskLunMapDao.getDiskIdByLunId(lunIdByVg) == null) {
                    lunsByVgWithNoDisks.add(lunIdByVg);
                }
            }
        } else {
            lunsByVgWithNoDisks.add(lunId);
        }

        List<StorageServerConnections> toRemove = new ArrayList<>();
        for (StorageServerConnections connection : connections) {
            fillConnectionDetailsIfNeeded(connection);
            if (connection.getId() != null) {
                List<String> list = lunDao
                        .getAllForStorageServerConnection(connection.getId())
                        .stream()
                        .map(LUNs::getLUNId)
                        .collect(Collectors.toList());

                if (0 < CollectionUtils.subtract(list, lunsByVgWithNoDisks).size()) {
                    toRemove.add(connection);
                }
            }
        }
        return (List<StorageServerConnections>) CollectionUtils.subtract(connections, toRemove);
    }

    public StorageServerConnections findConnectionWithSameDetails(StorageServerConnections connection) {
        // As we encrypt the password when saving the connection to the DB and each encryption generates different
        // result,
        // we can't query the connections to check if connection with the exact
        // same details was already added - so we query the connections with the same (currently relevant) details and
        // then compare the password after it was already
        // decrypted.
        // NOTE- THIS METHOD IS CURRENTLY USED ALSO FOR FCP connections, change with care.
        List<StorageServerConnections> connections = storageServerConnectionDao.getAllForConnection(connection);
        for (StorageServerConnections dbConnection : connections) {
            if (Objects.equals(dbConnection.getPassword(), connection.getPassword())) {
                return dbConnection;
            }
        }

        return null;
    }

    private void fillConnectionDetailsIfNeeded(StorageServerConnections connection) {
        // in case that the connection id is null (in case it wasn't loaded from the db before) - we can attempt to load
        // it from the db by its details.
        if (connection.getId() == null) {
            StorageServerConnections dbConnection = findConnectionWithSameDetails(connection);
            if (dbConnection != null) {
                connection.setId(dbConnection.getId());
            }
        }
    }

    @Override
    public boolean isConnectSucceeded(final Map<String, String> returnValue,
            List<StorageServerConnections> connections) {
        boolean result = true;
        List<String> failedConnectionsList =
                returnValue.keySet().stream().filter(a -> !"0".equals(returnValue.get(a))).collect(Collectors.toList());
        for (String failedConnection : failedConnectionsList) {
            List<LUNs> failedLuns = lunDao.getAllForStorageServerConnection(failedConnection);
            if (!failedLuns.isEmpty()) {
                for (LUNs lun : failedLuns) {
                    // TODO: check if LUNs in the same pool.
                    List<String> strings =
                            storageServerConnectionLunMapDao
                                    .getAll(lun.getLUNId())
                                    .stream()
                                    .map(LUNStorageServerConnectionMap::getStorageServerConnection)
                                    .collect(Collectors.toList());
                    if (CollectionUtils.subtract(strings, failedConnectionsList).size() == 0) {
                        // At case of failure the appropriate log message will be
                        // added
                        log.info("The lun with id '{}' was reported as problematic", lun.getPhysicalVolumeId());
                        for (String connectionFailed : failedConnectionsList) {
                            String connectionField =
                                    addToAuditLogErrorMessage(connectionFailed,
                                            returnValue.get(connectionFailed),
                                            connections, lun);
                            printLog(log, connectionField, returnValue.get(connectionFailed));
                        }
                        return false;
                    }
                }
            } else {
                result = false;
                printLog(log, failedConnection, returnValue.get(failedConnection));
            }
        }
        return result;
    }

    @Override
    public boolean storageDomainRemoved(StorageDomainStatic storageDomain) {
        List<StorageServerConnections> list =
                storageServerConnectionDao.getAllForVolumeGroup(storageDomain.getStorage());
        for (StorageServerConnections connection : filterConnectionsUsedByOthers(list, storageDomain.getStorage(), "")) {
            storageServerConnectionDao.remove(connection.getId());
        }

        // There is no need to remove entries from lun_storage_server_connection_map,
        // as the foreign key from the luns table is defined as ON DELETE CASCADE.
        removeStorageDomainLuns(storageDomain);

        return true;
    }

    @Override
    public boolean connectStorageToDomainByVdsId(StorageDomain storageDomain, Guid vdsId) {
        return runConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.ConnectStorageServer.getValue()).getFirst();
    }

    @Override
    public Pair<Boolean, EngineFault> connectStorageToDomainByVdsIdDetails(StorageDomain storageDomain, Guid vdsId) {
        return runConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.ConnectStorageServer.getValue());
    }

    @Override
    public boolean disconnectStorageFromDomainByVdsId(StorageDomain storageDomain, Guid vdsId) {
        return runConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.DisconnectStorageServer.getValue()).getFirst();
    }

    @Override
    public boolean connectStorageToLunByVdsId(StorageDomain storageDomain, Guid vdsId, LUNs lun, Guid storagePoolId) {
        return runConnectionStorageToDomain(storageDomain,
                vdsId,
                VDSCommandType.ConnectStorageServer.getValue(),
                lun,
                storagePoolId).getFirst();
    }

    @Override
    public boolean disconnectStorageFromLunByVdsId(StorageDomain storageDomain, Guid vdsId, LUNs lun) {
        return runConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.DisconnectStorageServer.getValue(),
                lun, Guid.Empty).getFirst();
    }

    @Override
    public boolean syncDomainInfo(StorageDomain storageDomain, Guid vdsId) {
        // Synchronize LUN details comprising the storage domain with the DB
        SyncLunsInfoForBlockStorageDomainParameters parameters = new SyncLunsInfoForBlockStorageDomainParameters(
                storageDomain.getId(), vdsId);
        return backend.runInternalAction(ActionType.SyncLunsInfoForBlockStorageDomain, parameters).getSucceeded();
    }
}
