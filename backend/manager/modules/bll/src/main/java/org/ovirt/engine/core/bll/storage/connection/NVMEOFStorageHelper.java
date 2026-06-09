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
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.SyncLunsInfoForBlockStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDao;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NVMEOFStorageHelper extends StorageHelperBase {
    private static final Logger log = LoggerFactory.getLogger(NVMEOFStorageHelper.class);

    @Inject
    private StorageServerConnectionLunMapDao storageServerConnectionLunMapDao;

    @Override
    public Collection<StorageType> getTypes() {
        return Collections.singleton(StorageType.NVMEOF);
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
            }
            Guid poolId = storagePoolId;
            if (storageDomain != null && storageDomain.getStoragePoolId() != null) {
                poolId = storageDomain.getStoragePoolId();
            }
            returnValue = resourceManager
                    .runVdsCommand(
                            VDSCommandType.forValue(type),
                            new StorageServerConnectionManagementVDSParameters(vdsId,
                                    poolId, StorageType.NVMEOF, list));
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
    public boolean storageDomainRemoved(StorageDomainStatic storageDomain) {
        List<StorageServerConnections> list =
                storageServerConnectionDao.getAllForVolumeGroup(storageDomain.getStorage());
        for (StorageServerConnections connection : filterConnectionsUsedByOthers(list, storageDomain.getStorage(), "")) {
            storageServerConnectionDao.remove(connection.getId());
        }
        removeStorageDomainLuns(storageDomain);
        return true;
    }

    @Override
    public boolean syncDomainInfo(StorageDomain storageDomain, Guid vdsId) {
        SyncLunsInfoForBlockStorageDomainParameters parameters = new SyncLunsInfoForBlockStorageDomainParameters(
                storageDomain.getId(), vdsId);
        return backend.runInternalAction(ActionType.SyncLunsInfoForBlockStorageDomain, parameters).getSucceeded();
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
                    List<String> strings =
                            storageServerConnectionLunMapDao
                                    .getAll(lun.getLUNId())
                                    .stream()
                                    .map(LUNStorageServerConnectionMap::getStorageServerConnection)
                                    .collect(Collectors.toList());
                    if (CollectionUtils.subtract(strings, failedConnectionsList).size() == 0) {
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
    protected List<StorageServerConnections> filterConnectionsUsedByOthers(
            List<StorageServerConnections> connections, String vgId, final String lunId) {
        List<String> lunsByVgWithNoDisks = new ArrayList<>();
        if (lunId.isEmpty()) {
            List<String> lunsByVg =
                    lunDao.getAllForVolumeGroup(vgId).stream().map(LUNs::getLUNId).collect(Collectors.toList());
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
        List<StorageServerConnections> connections = storageServerConnectionDao.getAllForStorage(connection.getConnection());
        for (StorageServerConnections dbConnection : connections) {
            if (dbConnection.getStorageType() == StorageType.NVMEOF
                    && Objects.equals(dbConnection.getNqn(), connection.getNqn())
                    && Objects.equals(dbConnection.getTrsvcid(), connection.getTrsvcid())
                    && Objects.equals(dbConnection.getHostNqn(), connection.getHostNqn())) {
                return dbConnection;
            }
        }
        return null;
    }
