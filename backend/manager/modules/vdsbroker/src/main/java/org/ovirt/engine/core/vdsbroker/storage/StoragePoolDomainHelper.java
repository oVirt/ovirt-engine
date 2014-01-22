package org.ovirt.engine.core.vdsbroker.storage;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;


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
                    domain.getStatus() == StorageDomainStatus.PreparingForMaintenance ) {
                storageDomains.put(domain.getstorage_id().toString(), "attached");
            } else {
                storageDomains.put(domain.getstorage_id().toString(),
                        StorageDomainStatus.Active.toString().toLowerCase());
            }
        }

        return storageDomains;
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
