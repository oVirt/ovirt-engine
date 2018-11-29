package org.ovirt.engine.core.bll.storage.domain;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.DetachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;

public class ForceRemoveStorageDomainCommand<T extends StorageDomainParametersBase> extends StorageDomainCommandBase<T> {

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private VmDao vmDao;

    private Boolean lastInStoragePool = null;

    public ForceRemoveStorageDomainCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected void executeCommand() {
        if (isAttachedStorageDomain() && hasRunningHostsInPool() && !isLastStorageInPool()) {
            try {
                // If master and there are more storage domains in the DC try to reconstruct.
                if (getStorageDomain().getStorageDomainType() == StorageDomainType.Master) {
                    ReconstructMasterParameters reconstructMasterParameters =
                            new ReconstructMasterParameters(getStoragePool().getId(),
                                    getStorageDomain().getId(), false);
                    reconstructMasterParameters.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
                    runInternalAction(ActionType.ReconstructMasterDomain, reconstructMasterParameters);
                }

                // try to force detach first
                DetachStorageDomainVDSCommandParameters detachParameters = new DetachStorageDomainVDSCommandParameters(
                        getStoragePool().getId(), getStorageDomain().getId(), Guid.Empty, -1);
                detachParameters.setForce(true);
                runVdsCommand(VDSCommandType.DetachStorageDomain, detachParameters);
            } catch (RuntimeException ex) {
                log.error("Could not force detach storage domain '{}': {}",
                        getStorageDomain().getStorageName(),
                        ex.getMessage());
                log.debug("Exception", ex);
            }
        } else {
            log.info("Avoid running host operations like reconstruct/detach on force remove for storage domain '{}'." +
                    "Storage domain attached to pool: '{}'" +
                    "Active Hosts exists in DC: '{}'",
                    getStorageDomain().getStorageName(),
                    isAttachedStorageDomain(),
                    hasRunningHostsInPool());
        }
        releaseStorageDomainMacPool(getVmsOnlyOnStorageDomain());
        storageHelperDirector.getItem(getStorageDomain().getStorageType())
                .storageDomainRemoved(getStorageDomain().getStorageStaticData());

        storageDomainDao.remove(getStorageDomain().getId());

        if (isAttachedStorageDomain()) {
            // if iso reset path for pool
            if (getStorageDomain().getStorageDomainType() == StorageDomainType.ISO) {
                // todo: when iso in multiple pools will be implemented, we
                // should reset iso path for all related pools
                runVdsCommand(VDSCommandType.ResetISOPath,
                        new IrsBaseVDSCommandParameters(getStoragePool().getId()));
            }
            if (getStorageDomain().getStorageDomainType() == StorageDomainType.Master) {
                calcStoragePoolStatusByDomainsStatus();
            }
        }
        setSucceeded(true);
    }

    private boolean hasRunningHostsInPool() {
        return getAllRunningVdssInPool().size() > 0;
    }

    private boolean isAttachedStorageDomain() {
        return getStoragePool() != null;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_FORCE_REMOVE_STORAGE_DOMAIN
                : AuditLogType.USER_FORCE_REMOVE_STORAGE_DOMAIN_FAILED;
    }

    @Override
    protected boolean validate() {
        boolean returnValue =
                super.validate() && checkStorageDomain()
                        && checkStorageDomainStatusNotEqual(StorageDomainStatus.Active);

        if (returnValue && getStorageDomain().getStorageDomainType() == StorageDomainType.Master
                && isAttachedStorageDomain() && !isLastStorageInPool()) {
            if (electNewMaster() == null) {
                returnValue = false;
                addValidationMessage(EngineMessage.ERROR_CANNOT_DESTROY_LAST_STORAGE_DOMAIN);
            } else if (!initializeVds()) {
                returnValue = false;
                addValidationMessage(EngineMessage.ERROR_CANNOT_DESTROY_LAST_STORAGE_DOMAIN_HOST_NOT_ACTIVE);
            }
        }

        if (returnValue && getStorageDomain().getStorageType() == StorageType.GLANCE) {
            addValidationMessage(EngineMessage.ERROR_CANNOT_MANAGE_STORAGE_DOMAIN);
            returnValue = false;
        }

        if (!isSupportedByManagedBlockStorageDomain(getStorageDomain())) {
            returnValue = false;
        }

        return returnValue;
    }

    private boolean isLastStorageInPool() {
        if (lastInStoragePool == null) {
            lastInStoragePool = storageDomainDao.getAllForStoragePool(getStoragePoolId()).size() == 1;
        }
        return lastInStoragePool.booleanValue();
    }
    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getStorageDomainId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__DESTROY_DOMAIN);
    }
}
