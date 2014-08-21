package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class ISCSIStorageHelper extends StorageHelperBase {

    @Override
    protected boolean runConnectionStorageToDomain(StorageDomain storageDomain, Guid vdsId, int type) {
        return runConnectionStorageToDomain(storageDomain, vdsId, type, null, Guid.Empty);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean runConnectionStorageToDomain(StorageDomain storageDomain,
            Guid vdsId,
            int type,
            LUNs lun,
            Guid storagePoolId) {
        boolean isSuccess = true;
        List<StorageServerConnections> list =
                (lun == null) ? DbFacade.getInstance()
                        .getStorageServerConnectionDao().getAllForVolumeGroup(storageDomain.getStorage())
                        : lun.getLunConnections();

        if (list.size() != 0) {
            if (VDSCommandType.forValue(type) == VDSCommandType.DisconnectStorageServer) {
                list = filterConnectionsUsedByOthers(list, storageDomain.getStorage(), lun != null ? lun.getLUN_id()
                        : "");
            } else if (VDSCommandType.forValue(type) == VDSCommandType.ConnectStorageServer) {
                list = updateIfaces(list, vdsId);
            }
            Guid poolId = storagePoolId;
            if (storageDomain != null && storageDomain.getStoragePoolId() != null) {
                poolId = storageDomain.getStoragePoolId();
            }
            VDSReturnValue returnValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.forValue(type),
                            new StorageServerConnectionManagementVDSParameters(vdsId,
                                    poolId, StorageType.ISCSI, list));
            isSuccess = returnValue.getSucceeded();
            if (isSuccess && VDSCommandType.forValue(type) == VDSCommandType.ConnectStorageServer) {
                isSuccess = isConnectSucceeded((Map<String, String>) returnValue.getReturnValue(), list);
            }
        }
        return isSuccess;
    }

    public static List<StorageServerConnections> updateIfaces(List<StorageServerConnections> conns, Guid vdsId) {
        List<StorageServerConnections> res = new ArrayList<>(conns);

        for (StorageServerConnections conn : conns) {
            // Get list of endpoints (nics or vlans) that will initiate iscsi sessions.
            // Targets are represented by StorageServerConnections object (connection, iqn, port, portal).
            List<VdsNetworkInterface> ifaces = DbFacade.getInstance().getInterfaceDao()
                    .getIscsiIfacesByHostIdAndStorageTargetId(vdsId, conn.getid());

            if (!ifaces.isEmpty()) {
                conn.setIface(ifaces.remove(0).getName());

                // Iscsi target is represented by connection object, therefore if this target is approachable
                // from more than one endpoint(initiator) we have to clone this connection per endpoint.
                for (VdsNetworkInterface iface : ifaces) {
                    StorageServerConnections newConn = StorageServerConnections.copyOf(conn);
                    newConn.setid(Guid.newGuid().toString());
                    newConn.setIface(iface.getName());
                    res.add(newConn);
                }
            }
        }

        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<StorageServerConnections> filterConnectionsUsedByOthers(
            List<StorageServerConnections> connections, String vgId, final String lunId) {
        // if we have lun id then filter by this lun
        // else get vg's luns from db
        List<String> lunsByVg =
                lunId.isEmpty() ? LinqUtils.transformToList(DbFacade.getInstance().getLunDao().getAllForVolumeGroup(vgId),
                        new Function<LUNs, String>() {
                            @Override
                            public String eval(LUNs a) {
                                return a.getLUN_id();
                            }
                        }) : null;
        // if a luns were retrieved by vgId, they can belongs not only to storage but also to disks
        // at that case they should left at db
        List<String> lunsByVgWithNoDisks = new ArrayList<String>();
        if (lunId.isEmpty()) {
            for (String lunIdByVg : lunsByVg) {
                if (DbFacade.getInstance().getDiskLunMapDao().getDiskIdByLunId(lunIdByVg) == null) {
                    lunsByVgWithNoDisks.add(lunIdByVg);
                }
            }
        } else {
            lunsByVgWithNoDisks.add(lunId);
        }

        List<StorageServerConnections> toRemove =
                new ArrayList<StorageServerConnections>();
        for (StorageServerConnections connection : connections) {
            fillConnectionDetailsIfNeeded(connection);
            if (connection.getid() != null) {
                List<String> list = LinqUtils.transformToList(
                        DbFacade.getInstance().getLunDao().getAllForStorageServerConnection(connection.getid()),
                        new Function<LUNs, String>() {
                            @Override
                            public String eval(LUNs a) {
                                return a.getLUN_id();
                            }
                        });

                if (0 < CollectionUtils.subtract(list, lunsByVgWithNoDisks).size()) {
                    toRemove.add(connection);
                }
            }
        }
        return (List<StorageServerConnections>) CollectionUtils.subtract(connections, toRemove);
    }

    private void fillConnectionDetailsIfNeeded(StorageServerConnections connection) {
        // in case that the connection id is null (in case it wasn't loaded from the db before) - we can attempt to load
        // it from the db by its details.
        if (connection.getid() == null) {
            List<StorageServerConnections> dbConnections = DbFacade.getInstance().getStorageServerConnectionDao().getAllForConnection(connection);
            if (!dbConnections.isEmpty()) {
                connection.setid(dbConnections.get(0).getid());
            }
        }
    }

    @Override
    public boolean isConnectSucceeded(final Map<String, String> returnValue,
            List<StorageServerConnections> connections) {
        boolean result = true;
        List<String> failedConnectionsList = LinqUtils.filter(returnValue.keySet(), new Predicate<String>() {
            @Override
            public boolean eval(String a) {
                return !"0".equals(returnValue.get(a));
            }
        });
        for (String failedConnection : failedConnectionsList) {
            List<LUNs> failedLuns = DbFacade.getInstance().getLunDao()
                    .getAllForStorageServerConnection(failedConnection);
            if (!failedLuns.isEmpty()) {
                for (LUNs lun : failedLuns) {
                    /**
                     * TODO: Vitaly check if luns in the same pool.
                     */
                    List<String> strings =
                            LinqUtils.transformToList(
                                    DbFacade.getInstance()
                                            .getStorageServerConnectionLunMapDao()
                                            .getAll(lun.getLUN_id()),
                                    new Function<LUN_storage_server_connection_map, String>() {
                                        @Override
                                        public String eval(LUN_storage_server_connection_map a) {
                                            return a.getstorage_server_connection();
                                        }
                                    });
                    if (CollectionUtils.subtract(strings, failedConnectionsList).size() == 0) {
                        // At case of failure the appropriate log message will be
                        // added
                        log.infoFormat("The lun with id {0} was reported as problematic !", lun.getphysical_volume_id());
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
        List<StorageServerConnections> list = DbFacade.getInstance()
                .getStorageServerConnectionDao().getAllForVolumeGroup(storageDomain.getStorage());
        for (StorageServerConnections connection : filterConnectionsUsedByOthers(list, storageDomain.getStorage(), "")) {
            DbFacade.getInstance().getStorageServerConnectionDao().remove(connection.getid());
        }

        // There is no need to remove entries from lun_storage_server_connection_map,
        // as the foreign key from the luns table is defined as ON DELETE CASCADE.
        removeStorageDomainLuns(storageDomain);

        return true;
    }

    @Override
    public boolean connectStorageToDomainByVdsId(StorageDomain storageDomain, Guid vdsId) {
        return runConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.ConnectStorageServer.getValue());
    }

    @Override
    public boolean disconnectStorageFromDomainByVdsId(StorageDomain storageDomain, Guid vdsId) {
        return runConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.DisconnectStorageServer.getValue());
    }

    @Override
    public boolean connectStorageToLunByVdsId(StorageDomain storageDomain, Guid vdsId, LUNs lun, Guid storagePoolId) {
        return runConnectionStorageToDomain(storageDomain,
                vdsId,
                VDSCommandType.ConnectStorageServer.getValue(),
                lun,
                storagePoolId);
    }

    @Override
    public boolean disconnectStorageFromLunByVdsId(StorageDomain storageDomain, Guid vdsId, LUNs lun) {
        return runConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.DisconnectStorageServer.getValue(),
                lun, Guid.Empty);
    }

    @Override
    public List<StorageServerConnections> getStorageServerConnectionsByDomain(
            StorageDomainStatic storageDomain) {
        return DbFacade.getInstance().getStorageServerConnectionDao().getAllForVolumeGroup(storageDomain.getStorage());
    }

    @Override
    public boolean syncDomainInfo(StorageDomain storageDomain, Guid vdsId) {
        // Synchronize LUN details comprising the storage domain with the DB
        StorageDomainParametersBase parameters = new StorageDomainParametersBase(storageDomain.getId());
        parameters.setVdsId(vdsId);
        return Backend.getInstance().runInternalAction(VdcActionType.SyncLunsInfoForBlockStorageDomain, parameters).getSucceeded();
    }
}
