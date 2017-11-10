package org.ovirt.engine.core.bll.storage.pool;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.StorageDomainCommandBase;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventResult;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;

@NonTransactiveCommandAttribute
public class RecoveryStoragePoolCommand extends StorageDomainCommandBase<ReconstructMasterParameters> {

    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private StoragePoolDao storagePoolDao;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public RecoveryStoragePoolCommand(Guid commandId) {
        super(commandId);
    }

    public RecoveryStoragePoolCommand(ReconstructMasterParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties;
    }

    @Override
    public Guid getStorageDomainId() {
        super.setStorageDomainId(getMasterDomainIdFromDb());
        return super.getStorageDomainId();
    }

    @Override
    protected boolean validate() {
        StoragePoolValidator storagePoolValidator = createStoragePoolValidator();
        if (!validate(storagePoolValidator.exists())
                || !validate(new StorageDomainValidator(getStorageDomain()).isInProcess())
                || !validate(storagePoolValidator.isAnyDomainInProcess())
                || !validate(storagePoolValidator.isNotInStatus(StoragePoolStatus.Uninitialized))) {
            return false;
        }

        if (getStorageDomain() != null && getStorageDomain().getStatus() == StorageDomainStatus.Active) {
            addStorageDomainStatusIllegalMessage();
            return false;
        }

        if (electNewMaster() != null) {
            return failValidation(EngineMessage.STORAGE_POOL_REINITIALIZE_WITH_MORE_THAN_ONE_DATA_DOMAIN);
        }

        StorageDomain domain = loadTargetedMasterDomain();
        if (domain.getStorageDomainSharedStatus() != StorageDomainSharedStatus.Unattached) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
        }

        return initializeVds();
    }

    private StorageDomain loadTargetedMasterDomain() {
        return storageDomainDao.get(getParameters().getNewMasterDomainId());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__RECOVER_POOL);
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__DOMAIN);
    }

    @Override
    protected void executeCommand() {
        StorageDomain newMasterDomain = loadTargetedMasterDomain();

        if (storageHelperDirector.getItem(newMasterDomain.getStorageType())
                .connectStorageToDomainByVdsId(newMasterDomain, getVds().getId())) {

            getEventQueue().submitEventSync(new Event(getParameters().getStoragePoolId(),
                            getParameters().getNewMasterDomainId(),
                            null,
                            EventType.RECOVERY, ""),
                    () -> {
                        getParameters().setStorageDomainId(getMasterDomainIdFromDb());
                        StoragePoolIsoMap domainPoolMap =
                                new StoragePoolIsoMap(
                                        getParameters().getNewMasterDomainId(),
                                        getParameters().getStoragePoolId(),
                                        StorageDomainStatus.Active);
                        storagePoolIsoMapDao.save(domainPoolMap);

                        getParameters().setVdsId(getVds().getId());
                        ActionReturnValue returnVal = runInternalAction(
                                ActionType.ReconstructMasterDomain, getParameters(), cloneContextAndDetachFromParent());

                        boolean reconstructVerbExecuted = (returnVal.getActionReturnValue() != null) ?
                                (Boolean) returnVal.getActionReturnValue() : false;

                        storagePoolDao.updateStatus(getStoragePool().getId(), StoragePoolStatus.NonResponsive);

                        if (!reconstructVerbExecuted) {
                            storagePoolIsoMapDao.remove(domainPoolMap.getId());
                        }

                        if (returnVal.getSucceeded()) {
                            updateStorageDomainFormatIfNeeded(loadTargetedMasterDomain());
                        }

                        setSucceeded(returnVal.getSucceeded());
                        return new EventResult(reconstructVerbExecuted, EventType.RECONSTRUCT);
                    });
        } else {
            getReturnValue().setFault(new EngineFault(new EngineException(EngineError.StorageServerConnectionError,
                    "Failed to connect storage"),
                    EngineError.StorageServerConnectionError));
        }
    }
}
