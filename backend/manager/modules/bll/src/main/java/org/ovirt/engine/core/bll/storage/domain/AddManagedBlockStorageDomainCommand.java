package org.ovirt.engine.core.bll.storage.domain;

import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.storage.ManagedBlockStorageDomainValidator;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddManagedBlockStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ManagedBlockStorageDao;
import org.ovirt.engine.core.dao.StorageDomainDynamicDao;

public class AddManagedBlockStorageDomainCommand<T extends AddManagedBlockStorageDomainParameters> extends AddStorageDomainCommand<T> {

    @Inject
    private ManagedBlockStorageDao managedBlockStorageDao;

    @Inject
    private StorageDomainDynamicDao storageDomainDynamicDao;

    public AddManagedBlockStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public void init() {
        super.init();
        AddManagedBlockStorageDomainParameters parameters = getParameters();
        parameters.getStorageDomain().setStorage(Guid.Empty.toString());
        parameters.getStorageDomain().setStorageFormat(StorageFormatType.V1);
        parameters.getStorageDomain().setDiscardAfterDelete(false);
        parameters.getStorageDomain().setBackup(false);
        parameters.getStorageDomain().setWipeAfterDelete(false);
    }

    @Override
    protected boolean validate() {
        return canAddDomain();
    }

    public AddManagedBlockStorageDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        initializeStorageDomain();
        addStorageDomainInDb();
        AddManagedBlockStorageDomainParameters parameters = getParameters();
        ManagedBlockStorage managedBlockStorage = new ManagedBlockStorage();
        managedBlockStorage.setId(getStorageDomainId());
        managedBlockStorage.setDriverOptions(parameters.getDriverOptions());
        managedBlockStorage.setDriverSensitiveOptions(parameters.getDriverSensitiveOptions());
        managedBlockStorageDao.save(managedBlockStorage);
        persistInitialStats();
        setSucceeded(true);
    }

    /**
     * Persist initial capacity stats for the new MBS domain by re-invoking
     * GetManagedBlockStorageStats and writing the parsed result to
     * storage_domain_dynamic. Without this the UI shows [N/A] for Total/Free
     * because the SPM-style storage monitor doesn't cover MBS domains.
     *
     * Failure here is non-fatal: the domain is already created and Active;
     * stats can be refreshed later (engine startup hook, on-demand action).
     */
    private void persistInitialStats() {
        try {
            ActionReturnValue rv = runInternalAction(
                    ActionType.GetManagedBlockStorageStats, getParameters());
            if (rv == null || !rv.getSucceeded() || rv.getActionReturnValue() == null) {
                log.warn("Could not retrieve initial stats for MBS domain '{}'",
                        getStorageDomainId());
                return;
            }
            Map<String, Object> stats = rv.getActionReturnValue();
            Integer totalGb = toIntOrNull(stats.get("total_capacity_gb"));
            Integer freeGb = toIntOrNull(stats.get("free_capacity_gb"));
            if (totalGb == null || freeGb == null) {
                log.warn("Stats for MBS domain '{}' missing capacity fields: {}",
                        getStorageDomainId(), stats);
                return;
            }
            StorageDomainDynamic dynamic = storageDomainDynamicDao.get(getStorageDomainId());
            if (dynamic == null) {
                dynamic = new StorageDomainDynamic();
                dynamic.setId(getStorageDomainId());
            }
            dynamic.setAvailableDiskSize(freeGb);
            dynamic.setUsedDiskSize(totalGb - freeGb);
            storageDomainDynamicDao.update(dynamic);
            log.info("Persisted initial capacity for MBS domain '{}': total={}GB free={}GB",
                    getStorageDomainId(), totalGb, freeGb);
        } catch (Exception e) {
            log.warn("Could not persist initial stats for MBS domain '{}': {}",
                    getStorageDomainId(), e.getMessage());
        }
    }

    private static Integer toIntOrNull(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        try {
            return Integer.parseInt(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    protected boolean canAddDomain() {
        if (!validate(ManagedBlockStorageDomainValidator.isOperationSupportedByManagedBlockStorage(getActionType()))) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_MANAGEDBLOCK_DATA_BASE_REQUIRED);
        }
        return testStorageConnection();
    }

    private boolean testStorageConnection() {
        ActionReturnValue actionReturnValue = runInternalAction(ActionType.GetManagedBlockStorageStats, getParameters());
        return actionReturnValue.getSucceeded() && actionReturnValue.getActionReturnValue() != null ||
                failValidation(EngineMessage.FAILED_TO_CONNECT_MANAGED_BLOCK_DOMAIN);
    }

    @Override
    public void setPropertiesForAuditLog(AuditLog auditLog) {

    }

    @Override
    public void updateCallStackFromThrowable(Throwable throwable) {

    }
}
