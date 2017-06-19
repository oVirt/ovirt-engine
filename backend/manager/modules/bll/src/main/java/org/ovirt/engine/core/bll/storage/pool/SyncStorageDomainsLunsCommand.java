package org.ovirt.engine.core.bll.storage.pool;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.SyncLunsInfoForBlockStorageDomainParameters;
import org.ovirt.engine.core.common.action.SyncLunsParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.StorageDomainDao;

/**
 * This command is responsible for synchronizing all the active block storage domains in the storage pool.
 */
@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class SyncStorageDomainsLunsCommand<T extends SyncLunsParameters> extends AbstractSyncLunsCommand<T> {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private StorageDomainDao storageDomainDao;

    public SyncStorageDomainsLunsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        List<Guid> unSyncedStorageDomains = syncStorageDomains();
        if (!unSyncedStorageDomains.isEmpty()) {
            addCustomValue("StorageDomainsIds",
                    unSyncedStorageDomains.stream().map(Guid::toString).collect(Collectors.joining(", ")));
            auditLogDirector.log(this, AuditLogType.STORAGE_DOMAINS_COULD_NOT_BE_SYNCED);
        }

        setSucceeded(true);
    }

    protected Map<String, StorageDomain> getStorageDomainsInPoolByVirtualGroup() {
        return storageDomainDao.getAllForStoragePool(getParameters().getStoragePoolId())
                .stream()
                .filter(storageDomain -> storageDomain.getStatus() == StorageDomainStatus.Active)
                .filter(storageDomain -> storageDomain.getStorageType().isBlockDomain())
                .collect(Collectors.toMap(StorageDomain::getStorage, Function.identity()));
    }

    protected Map<Guid, List<LUNs>> getLunsGroupedByStorageDomainId() {
        Map<String, StorageDomain> storageDomainsByVirtualGroup = getStorageDomainsInPoolByVirtualGroup();
        return getDeviceList()
                .stream()
                .filter(lun -> storageDomainsByVirtualGroup.containsKey(lun.getVolumeGroupId()))
                .peek(lun -> lun.setStorageDomainId(storageDomainsByVirtualGroup.get(lun.getVolumeGroupId()).getId()))
                .collect(Collectors.groupingBy(LUNs::getStorageDomainId));
    }

    protected boolean runSyncLunsInfoForBlockStorageDomain(Guid storageDomainId,
            List<LUNs> storageDomainLuns) {
        return runInternalAction(ActionType.SyncLunsInfoForBlockStorageDomain,
                new SyncLunsInfoForBlockStorageDomainParameters(storageDomainId, storageDomainLuns)).getSucceeded();
    }

    /**
     * Runs SyncLunsInfoForBlockStorageDomainCommand for every storage domain in the storage pool,
     * and returns a list containing the IDs of the storage domains that have failed to sync.
     */
    protected List<Guid> syncStorageDomains() {
        return getLunsGroupedByStorageDomainId()
                .entrySet()
                .stream()
                .filter(sdLuns -> !runSyncLunsInfoForBlockStorageDomain(sdLuns.getKey(), sdLuns.getValue()))
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }
}
