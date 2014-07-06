package org.ovirt.engine.core.vdsbroker.storage;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.ResourceManager;


public class StoragePoolDomainHelper {

    private static final Log log = LogFactory.getLog(StoragePoolDomainHelper.class);

    public static final Set<StorageDomainStatus> storageDomainMonitoredStatus =
            Collections.unmodifiableSet(EnumSet.of(StorageDomainStatus.Inactive, StorageDomainStatus.Active));

    public static Map<String, String> buildStoragePoolDomainsMap(List<StoragePoolIsoMap> storagePoolIsoMaps) {
        Map<String, String> storageDomains = new HashMap<String, String>();

        for (StoragePoolIsoMap domain : storagePoolIsoMaps) {
            if (domain.getStatus() == StorageDomainStatus.Detaching) {
                continue;
            }
            if (domain.getStatus() == StorageDomainStatus.Maintenance ||
                    domain.getStatus() == StorageDomainStatus.PreparingForMaintenance ||
                    domain.getStatus() == StorageDomainStatus.Activating) {
                storageDomains.put(domain.getstorage_id().toString(), "attached");
            } else {
                storageDomains.put(domain.getstorage_id().toString(),
                        StorageDomainStatus.Active.toString().toLowerCase());
            }
        }

        return storageDomains;
    }

    /**
     * Refreshes the given vds pool metadata, if the host isn't connected to the pool it'll be connected.
     *
     * @return boolean indicating whether the host pool metadata was "refreshed" succesfully (either by refresh or
     *         connect)
     */
    public static boolean refreshHostPoolMetadata(VDS vds, StoragePool storagePool, Guid masterDomainId, List<StoragePoolIsoMap> storagePoolIsoMaps) {
        try {
            ResourceManager.getInstance().runVdsCommand(
                    VDSCommandType.ConnectStoragePool,
                    new ConnectStoragePoolVDSCommandParameters(vds,
                            storagePool,
                            masterDomainId, storagePoolIsoMaps, true));
        } catch (VdcBLLException ex) {
            VDSError error = ex.getVdsError();
            if (error.getCode() != VdcBllErrors.StoragePoolUnknown) {
                log.infoFormat("Failed to refresh host {0} pool {1} metadata with error {2} (message: {3})",
                        vds.getName(),
                        storagePool.getId(), error.getCode(), error.getMessage());
                return false;
            }

            error = null;

            try {
                VDSReturnValue vdsReturnValue = ResourceManager.getInstance().runVdsCommand(
                        VDSCommandType.ConnectStoragePool,
                        new ConnectStoragePoolVDSCommandParameters(vds,
                                storagePool,
                                masterDomainId, storagePoolIsoMaps, false));
                if (!vdsReturnValue.getSucceeded()) {
                    error = vdsReturnValue.getVdsError();
                }
            } catch (VdcBLLException e) {
                error = e.getVdsError();
            }

            if (error != null) {
                log.infoFormat("Failed to connect host {0} to pool {1} with error {2} (message: {3})",
                        vds.getName(),
                        storagePool.getId(), error.getCode(), error.getMessage());
                return false;
            }
        }

        return true;
    }

    public static void updateApplicablePoolDomainsStatuses(Guid storagePoolId,
            Set<StorageDomainStatus> applicableStatusesForUpdate,
            StorageDomainStatus newStatus, String reason) {
        List<StoragePoolIsoMap> storagesStatusInPool = DbFacade.getInstance()
                .getStoragePoolIsoMapDao().getAllForStoragePool(storagePoolId);
        for (StoragePoolIsoMap storageStatusInPool : storagesStatusInPool) {
            if (storageStatusInPool.getStatus() != null
                    && storageStatusInPool.getStatus() != newStatus
                    && applicableStatusesForUpdate.contains(storageStatusInPool.getStatus())) {
                log.infoFormat("Storage Pool {0} - Updating Storage Domain {1} status from {2} to {3}, reason : {4}",
                        storagePoolId,
                        storageStatusInPool.getstorage_id(),
                        storageStatusInPool.getStatus().name(),
                        newStatus.name(),
                        reason);
                storageStatusInPool.setStatus(newStatus);
                DbFacade.getInstance()
                        .getStoragePoolIsoMapDao()
                        .updateStatus(storageStatusInPool.getId(), storageStatusInPool.getStatus());
            }
        }
    }
}
