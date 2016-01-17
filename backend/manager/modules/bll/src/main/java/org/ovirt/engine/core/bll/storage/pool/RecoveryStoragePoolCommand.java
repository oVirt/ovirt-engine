package org.ovirt.engine.core.bll.storage.pool;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.StorageHelperDirector;
import org.ovirt.engine.core.bll.storage.domain.StorageDomainCommandBase;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
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
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@NonTransactiveCommandAttribute
public class RecoveryStoragePoolCommand extends StorageDomainCommandBase<ReconstructMasterParameters> {

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
        boolean returnValue = checkStoragePool();

        if (!validate(new StorageDomainValidator(getStorageDomain()).isInProcess())
                || !validate(new StoragePoolValidator(getStoragePool()).isAnyDomainInProcess())) {
            return false;
        }

        if (returnValue) {
            if (getStoragePool().getStatus() == StoragePoolStatus.Uninitialized) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
                return false;
            } else if (getStorageDomain() != null && getStorageDomain().getStatus() != null
                    && getStorageDomain().getStatus() == StorageDomainStatus.Active) {
                addStorageDomainStatusIllegalMessage();
                returnValue = false;
            } else if (electNewMaster() != null) {
                getReturnValue().getValidationMessages().add(
                        EngineMessage.STORAGE_POOL_REINITIALIZE_WITH_MORE_THAN_ONE_DATA_DOMAIN.toString());
                returnValue = false;
            } else {
                StorageDomain domain = loadTargetedMasterDomain();
                if (domain.getStorageDomainSharedStatus() != StorageDomainSharedStatus.Unattached) {
                    addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
                    returnValue = false;
                }
            }
        }

        return returnValue && initializeVds();
    }

    private StorageDomain loadTargetedMasterDomain() {
        return getStorageDomainDao().get(getParameters().getNewMasterDomainId());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__RECOVER_POOL);
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__DOMAIN);
    }

    @Override
    protected void executeCommand() {
        StorageDomain newMasterDomain = loadTargetedMasterDomain();

        if (StorageHelperDirector.getInstance().getItem(newMasterDomain.getStorageType())
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
                        DbFacade.getInstance()
                                .getStoragePoolIsoMapDao()
                                .save(domainPoolMap);

                        getParameters().setVdsId(getVds().getId());
                        VdcReturnValueBase returnVal = runInternalAction(
                                VdcActionType.ReconstructMasterDomain, getParameters(), cloneContextAndDetachFromParent());

                        boolean reconstructVerbExecuted = (returnVal.getActionReturnValue() != null) ?
                                (Boolean) returnVal.getActionReturnValue() : false;

                        getStoragePoolDao().updateStatus(getStoragePool().getId(),
                                StoragePoolStatus.NonResponsive);

                        if (!reconstructVerbExecuted) {
                            getStoragePoolIsoMapDao().remove(domainPoolMap.getId());
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
