package org.ovirt.engine.core.vdsbroker.storage;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class StoragePoolDomainHelper {

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    private static final Logger log = LoggerFactory.getLogger(StoragePoolDomainHelper.class);

    public static final Set<VDSStatus> vdsDomainsActiveMonitoringStatus = Collections.unmodifiableSet(EnumSet.of(VDSStatus.Up));

    public static final Set<VDSStatus> vdsDomainsMaintenanceMonitoringStatus =
            Collections.unmodifiableSet(EnumSet.of(
                    VDSStatus.Up,
                    VDSStatus.NonOperational));

    private static final Set<StorageDomainStatus> domainAttachedStatuses =
            Collections.unmodifiableSet(EnumSet.of(StorageDomainStatus.Maintenance,
                    StorageDomainStatus.Detaching, StorageDomainStatus.PreparingForMaintenance,
                    StorageDomainStatus.Activating));

    public static Map<String, String> buildStoragePoolDomainsMap(List<StoragePoolIsoMap> storagePoolIsoMaps) {
        return storagePoolIsoMaps.stream().collect(Collectors.toMap(x -> x.getStorageId().toString(),
                x -> domainAttachedStatuses.contains(x.getStatus()) ? "attached" : "active"));
    }

    /**
     * Refreshes the given vds pool metadata, if the host isn't connected to the pool it'll be connected.
     *
     */
    public void refreshHostPoolMetadata(VDS vds, StoragePool storagePool, Guid masterDomainId, List<StoragePoolIsoMap> storagePoolIsoMaps) {
        try {
            resourceManager.runVdsCommand(
                    VDSCommandType.ConnectStoragePool,
                    new ConnectStoragePoolVDSCommandParameters(vds,
                            storagePool,
                            masterDomainId, storagePoolIsoMaps, true));
        } catch (EngineException ex) {
            VDSError error = ex.getVdsError();
            if (error.getCode() != EngineError.StoragePoolUnknown) {
                log.info("Failed to refresh host '{}' pool '{}' metadata with error '{}': {}",
                        vds.getName(),
                        storagePool.getId(), error.getCode(), error.getMessage());
                log.debug("Exception", ex);
                return;
            }

            error = null;

            try {
                VDSReturnValue vdsReturnValue = resourceManager.runVdsCommand(
                        VDSCommandType.ConnectStoragePool,
                        new ConnectStoragePoolVDSCommandParameters(vds,
                                storagePool,
                                masterDomainId, storagePoolIsoMaps, false));
                if (!vdsReturnValue.getSucceeded()) {
                    error = vdsReturnValue.getVdsError();
                }
            } catch (EngineException e) {
                error = e.getVdsError();
            }

            if (error != null) {
                log.info("Failed to connect host '{}' to pool '{}' with error '{}': {}",
                        vds.getName(),
                        storagePool.getId(), error.getCode(), error.getMessage());
            }
        }

    }

    public void updateApplicablePoolDomainsStatuses(Guid storagePoolId,
            Set<StorageDomainStatus> applicableStatusesForUpdate,
            StorageDomainStatus newStatus, String reason) {
        List<StoragePoolIsoMap> storagesStatusInPool = storagePoolIsoMapDao.getAllForStoragePool(storagePoolId);
        for (StoragePoolIsoMap storageStatusInPool : storagesStatusInPool) {
            if (storageStatusInPool.getStatus() != null
                    && storageStatusInPool.getStatus() != newStatus
                    && applicableStatusesForUpdate.contains(storageStatusInPool.getStatus())) {
                log.info("Storage Pool '{}' - Updating Storage Domain '{}' status from '{}' to '{}', reason: {}",
                        storagePoolId,
                        storageStatusInPool.getStorageId(),
                        storageStatusInPool.getStatus().name(),
                        newStatus.name(),
                        reason);
                storageStatusInPool.setStatus(newStatus);
                storagePoolIsoMapDao.updateStatus(storageStatusInPool.getId(), storageStatusInPool.getStatus());
            }
        }
    }
}
