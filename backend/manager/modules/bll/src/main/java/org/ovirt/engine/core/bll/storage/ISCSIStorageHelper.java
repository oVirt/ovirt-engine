package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.vdscommands.ConnectStorageServerVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class ISCSIStorageHelper extends StorageHelperBase {

    protected static final Log log = LogFactory.getLog(ISCSIStorageHelper.class);

    @Override
    protected boolean RunConnectionStorageToDomain(storage_domains storageDomain, Guid vdsId, int type) {
        return RunConnectionStorageToDomain(storageDomain, vdsId, type, null, Guid.Empty);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean RunConnectionStorageToDomain(storage_domains storageDomain, Guid vdsId, int type, LUNs lun, Guid storagePoolId) {
        boolean isSuccess = true;
        List<StorageServerConnections> list =
                (lun == null) ? DbFacade.getInstance()
                        .getStorageServerConnectionDao().getAllForVolumeGroup(storageDomain.getstorage())
                        : lun.getLunConnections();

        if (list.size() != 0) {
            if (VDSCommandType.forValue(type) == VDSCommandType.DisconnectStorageServer) {
                list = filterConnectionsUsedByOthers(list, storageDomain.getstorage(), lun != null ? lun.getLUN_id()
                        : "");
            }
            Guid poolId = storagePoolId;
            if (storageDomain != null && storageDomain.getstorage_pool_id() != null) {
                poolId = storageDomain.getstorage_pool_id().getValue();
            }
            VDSReturnValue returnValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.forValue(type),
                            new ConnectStorageServerVDSCommandParameters(vdsId,
                                    poolId, StorageType.ISCSI, list));
            isSuccess = returnValue.getSucceeded();
            if (isSuccess && VDSCommandType.forValue(type) == VDSCommandType.ConnectStorageServer) {
                isSuccess = IsConnectSucceeded((Map<String, String>) returnValue.getReturnValue(), list);
            }
        }
        return isSuccess;
    }

    private List<StorageServerConnections> FilterConnectionsUsedByOthers(
            List<StorageServerConnections> connections, String vgId) {
        return filterConnectionsUsedByOthers(connections, vgId, "");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<StorageServerConnections> filterConnectionsUsedByOthers(
            List<StorageServerConnections> connections, String vgId, final String lunId) {
        // if we have lun id then filter by this lun
        // else get vg's luns from db
        List<String> lunsByVg =
                lunId.isEmpty() ? LinqUtils.foreach(DbFacade.getInstance().getLunDao().getAllForVolumeGroup(vgId),
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
            List<String> list = LinqUtils.foreach(
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
        return (List<StorageServerConnections>) CollectionUtils.subtract(connections, toRemove);
    }

    @Override
    public boolean ValidateStoragePoolConnectionsInHost(VDS vds, List<StorageServerConnections> connections,
            Guid storagePoolId) {
        if (connections.size() > 0) {
            @SuppressWarnings("unchecked")
            Map<String, String> validateConnections = (Map<String, String>) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.ValidateStorageServerConnection,
                            new ConnectStorageServerVDSCommandParameters(vds.getId(), storagePoolId,
                                    StorageType.ISCSI, connections)).getReturnValue();

            return IsConnectSucceeded(validateConnections, connections);
        }
        return true;
    }

    @Override
    public boolean IsConnectSucceeded(final Map<String, String> returnValue,
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
                            LinqUtils.foreach(
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
    public boolean StorageDomainRemoved(StorageDomainStatic storageDomain) {
        int numOfRemovedLuns = removeStorageDomainLuns(storageDomain);
        if (numOfRemovedLuns > 0) {
            List<StorageServerConnections> list = DbFacade.getInstance()
                    .getStorageServerConnectionDao().getAllForVolumeGroup(storageDomain.getstorage());
            for (StorageServerConnections connection : FilterConnectionsUsedByOthers(list, storageDomain.getstorage())) {
                DbFacade.getInstance().getStorageServerConnectionDao().remove(connection.getid());
            }
        }
        return true;
    }

    @Override
    public boolean ConnectStorageToDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId) {
        return RunForSingleConnectionInHost(storageDomain, storagePoolId,
                VDSCommandType.ConnectStorageServer.getValue());
    }

    @Override
    public boolean DisconnectStorageFromDomainByStoragePoolId(storage_domains storageDomain, Guid storagePoolId) {
        return RunForSingleConnectionInHost(storageDomain, storagePoolId,
                VDSCommandType.DisconnectStorageServer.getValue());
    }

    @Override
    public boolean ConnectStorageToDomainByVdsId(storage_domains storageDomain, Guid vdsId) {
        return RunConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.ConnectStorageServer.getValue());
    }

    @Override
    public boolean DisconnectStorageFromDomainByVdsId(storage_domains storageDomain, Guid vdsId) {
        return RunConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.DisconnectStorageServer.getValue());
    }

    @Override
    public boolean ConnectStorageToLunByVdsId(storage_domains storageDomain, Guid vdsId, LUNs lun, Guid storagePoolId) {
        return RunConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.ConnectStorageServer.getValue(), lun, storagePoolId);
    }

    @Override
    public boolean DisconnectStorageFromLunByVdsId(storage_domains storageDomain, Guid vdsId, LUNs lun) {
        return RunConnectionStorageToDomain(storageDomain, vdsId, VDSCommandType.DisconnectStorageServer.getValue(),
                lun, Guid.Empty);
    }

    @Override
    public List<StorageServerConnections> GetStorageServerConnectionsByDomain(
            StorageDomainStatic storageDomain) {
        return DbFacade.getInstance().getStorageServerConnectionDao().getAllForVolumeGroup(storageDomain.getstorage());
    }
}
