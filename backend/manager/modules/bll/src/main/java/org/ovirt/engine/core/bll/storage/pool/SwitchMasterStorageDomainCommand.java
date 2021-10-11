package org.ovirt.engine.core.bll.storage.pool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessage;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.StorageDomainCommandBase;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.SwitchMasterStorageDomainCommandParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.DeactivateStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.AsyncTaskDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class SwitchMasterStorageDomainCommand<T extends SwitchMasterStorageDomainCommandParameters>
        extends StorageDomainCommandBase<T> {

    @Inject
    private AsyncTaskDao asyncTaskDao;
    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(SwitchMasterStorageDomainCommandCallback.class)
    private Instance<SwitchMasterStorageDomainCommandCallback> callbackProvider;

    private StorageDomain currentMasterStorageDomain;

    public SwitchMasterStorageDomainCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public SwitchMasterStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public void init() {
        super.init();
        initializeCurrentMasterDomain();
    }

    private void initializeCurrentMasterDomain() {
        if (getParameters().getCurrentMasterStorageDomainId() == null) {
            getParameters().setCurrentMasterStorageDomainId(
                    storageDomainDao.getMasterStorageDomainIdForPool(getStoragePoolId()));
        }
    }

    @Override
    protected boolean validate() {
        if (getStoragePool() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
        }

        if (getStorageDomainId() == null || getStorageDomainId().equals(Guid.Empty)) {
            return failValidation(EngineMessage.STORAGE_DOMAIN_DOES_NOT_EXIST);
        }

        if (!FeatureSupported.isSwitchMasterStorageDomainOperationSupported(
                getStoragePool().getCompatibilityVersion())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_SWITCH_MASTER_STORAGE_DOMAIN_NOT_SUPPORTED);
        }

        if (getStorageDomainId().equals(getParameters().getCurrentMasterStorageDomainId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_MASTER);
        }

        StorageDomain newMasterSD = storageDomainDao.getForStoragePool(getStorageDomainId(), getStoragePoolId());
        if (newMasterSD == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_IN_STORAGE_POOL);
        }

        if (newMasterSD.getStatus() != StorageDomainStatus.Active &&
                newMasterSD.getStatus() != StorageDomainStatus.Unknown) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_MUST_BE_ACTIVE,
                    String.format("$domainName %1$s", newMasterSD.getName()));
        }

        if (newMasterSD.getStorageDomainType() != StorageDomainType.Data || newMasterSD.isBackup()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_ACTION_IS_SUPPORTED_ONLY_FOR_DATA_DOMAINS);
        }

        if (hasRunningTasks(getStoragePoolId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_HAS_RUNNING_TASKS);
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        if (!switchMasterDomain()) {
            setCommandStatus(CommandStatus.FAILED);
            return;
        }
        persistCommandIfNeeded();
        setSucceeded(true);
    }

    private boolean switchMasterDomain() {
        // Step 1 - try to switch master domain on vdsm.
        log.info("Locking the following storage domains: {}, {}",
                getCurrentMasterStorageDomain().getName(),
                getStorageDomain().getName());
        lockStorageDomain(getCurrentMasterStorageDomain());
        lockStorageDomain(getStorageDomain());

        DeactivateStorageDomainVDSCommandParameters switchMasterParameters =
                new DeactivateStorageDomainVDSCommandParameters(
                        getStoragePoolId(),
                        getStorageDomainId(),
                        getParameters().getCurrentMasterStorageDomainId(),
                        getStoragePool().getMasterDomainVersion() + 1
                );

        try {
            VDSReturnValue returnValue =
                    runVdsCommand(VDSCommandType.SwitchMasterStorageDomain, switchMasterParameters);
            if (returnValue.getSucceeded()) {
                Guid taskId = persistAsyncTaskPlaceHolder(getActionType());
                getTaskIdList().add(createTask(taskId,
                        returnValue.getCreationInfo(),
                        ActionType.SwitchMasterStorageDomain,
                        VdcObjectType.Storage,
                        getStorageDomainId()));
            }
        } catch (EngineException e) {
            log.error("Switching the master role to storage domain '{}' failed: {}", getStorageDomainId(), e);
            return false;
        }
        return true;
    }

    protected void updateStoragePoolOnDB() {
        // Step 2 - Update Engine and activate the storage domains.
        TransactionSupport.executeInNewTransaction(() -> {
            int masterDomainVersion = storagePoolDao.increaseStoragePoolMasterVersion(getStoragePool().getId());
            getStoragePool().setMasterDomainVersion(masterDomainVersion);
            updateRoleAndActivateDomain(getStorageDomain(), StorageDomainType.Master);
            updateRoleAndActivateDomain(getCurrentMasterStorageDomain(), StorageDomainType.Data);
            return null;
        });
    }

    protected void updateRoleAndActivateDomain(StorageDomain sd, StorageDomainType type) {
        log.info("Activating storage domain {}", sd.getName());
        sd.setStorageDomainType(type);
        StoragePoolIsoMap mapOfDomain = sd.getStoragePoolIsoMapData();
        mapOfDomain.setStatus(StorageDomainStatus.Active);
        storageDomainStaticDao.update(sd.getStorageStaticData());
        storagePoolIsoMapDao.updateStatus(mapOfDomain.getId(), mapOfDomain.getStatus());
    }

    protected StorageDomain getCurrentMasterStorageDomain() {
        if (currentMasterStorageDomain == null) {
            currentMasterStorageDomain = storageDomainDao.getForStoragePool(
                    getParameters().getCurrentMasterStorageDomainId(), getStoragePoolId());
        }
        return currentMasterStorageDomain;
    }

    protected boolean hasRunningTasks(Guid storagePoolId) {
        return !asyncTaskDao.getAsyncTaskIdsByStoragePoolId(storagePoolId).isEmpty();
    }

    private void addAuditLogCustomValues() {
        addCustomValue("OldMaster", getCurrentMasterStorageDomain().getName());
        addCustomValue("NewMaster", getStorageDomain().getName());
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__SWITCH_MASTER);
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__DOMAIN__SWITCH_MASTER);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = new HashMap<>();
            String oldMasterName = getCurrentMasterStorageDomain().getName();
            jobProperties.put("oldmaster", (oldMasterName == null) ? "" : oldMasterName);
            jobProperties.put("newmaster", (getStorageDomainName() == null) ? "" : getStorageDomainName());
        }
        return jobProperties;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissions = new ArrayList<>();
        permissions.add(new PermissionSubject(getStoragePoolId(),
                VdcObjectType.StoragePool,
                ActionGroup.MANIPULATE_STORAGE_DOMAIN));
        return permissions;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        LockMessage lockMessage = new LockMessage(EngineMessage.ACTION_TYPE_FAILED_MASTER_ROLE_IS_BEING_SWITCHED)
                .withOptional("currMaster", getParameters().getCurrentMasterStorageDomainId().toString())
                .withOptional("newMaster", getStorageDomainId().toString());

        locks.put(getStoragePoolId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.POOL, lockMessage));
        locks.put(getParameters().getCurrentMasterStorageDomainId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE, lockMessage));
        locks.put(getStorageDomainId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE, lockMessage));
        return locks;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.switchMaster;
    }

    @Override
    protected void endSuccessfully() {
        addAuditLogCustomValues();
        auditLogDirector.log(this, AuditLogType.SWITCH_MASTER_STORAGE_DOMAIN_ON_SPM);
        updateStoragePoolOnDB();
        commandCoordinatorUtil.removeAllCommandsInHierarchy(getCommandId());
        auditLogDirector.log(this, AuditLogType.SWITCH_MASTER_STORAGE_DOMAIN);
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        // Unlock the entities and ReconstructMaster is being called if necessary
        addAuditLogCustomValues();
        updateRoleAndActivateDomain(getStorageDomain(), StorageDomainType.Data);
        updateRoleAndActivateDomain(getCurrentMasterStorageDomain(), StorageDomainType.Master);
        commandCoordinatorUtil.removeAllCommandsInHierarchy(getCommandId());
        auditLogDirector.log(this, AuditLogType.SWITCH_MASTER_STORAGE_DOMAIN_FAILED);
        super.endWithFailure();
    }
}
