package org.ovirt.engine.core.bll.storage.pool;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.SyncLunsInfoForBlockStorageDomainParameters;
import org.ovirt.engine.core.common.action.SyncLunsParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

public abstract class AbstractSyncStorageDomainsLunsCommand<T extends SyncLunsParameters>
        extends AbstractSyncLunsCommand<T> {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private VdsCommandsHelper vdsCommandsHelper;

    public AbstractSyncStorageDomainsLunsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
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

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__DOMAIN);
    }

    /**
     * Synchronizes the storage domains and returns a list containing
     * the IDs of the storage domains that have failed to sync.
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

    protected Map<Guid, List<LUNs>> getLunsGroupedByStorageDomainId() {
        Map<String, StorageDomain> storageDomainsByVirtualGroup = getStorageDomainsToSync()
                .collect(Collectors.toMap(StorageDomain::getStorage, Function.identity()));
        return getDeviceList()
                .stream()
                .filter(lun -> storageDomainsByVirtualGroup.containsKey(lun.getVolumeGroupId()))
                .peek(lun -> lun.setStorageDomainId(storageDomainsByVirtualGroup.get(lun.getVolumeGroupId()).getId()))
                .collect(Collectors.groupingBy(LUNs::getStorageDomainId));
    }

    protected boolean runSyncLunsInfoForBlockStorageDomain(Guid storageDomainId,
            List<LUNs> storageDomainLuns) {
        return runInternalAction(ActionType.SyncLunsInfoForBlockStorageDomain,
                new SyncLunsInfoForBlockStorageDomainParameters(
                        storageDomainId, getHostToSyncStorageDomainsLuns(), storageDomainLuns))
                .getSucceeded();
    }

    private Guid getHostToSyncStorageDomainsLuns() {
        if (Guid.isNullOrEmpty(getParameters().getStoragePoolId())) {
            return getParameters().getVdsId();
        }
        return vdsCommandsHelper.getHostForExecution(getParameters().getStoragePoolId());
    }

    protected abstract Stream<StorageDomain> getStorageDomainsToSync();
}
