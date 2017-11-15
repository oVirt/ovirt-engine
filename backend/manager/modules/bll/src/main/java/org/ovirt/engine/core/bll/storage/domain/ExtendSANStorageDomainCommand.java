package org.ovirt.engine.core.bll.storage.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.ConnectAllHostsToLunCommand.ConnectAllHostsToLunResult;
import org.ovirt.engine.core.bll.storage.utils.BlockStorageDiscardFunctionalityHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ExtendStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetVGInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ExtendSANStorageDomainCommand<T extends ExtendSANStorageDomainParameters> extends
        StorageDomainCommandBase<T> {

    @Inject
    private BlockStorageDiscardFunctionalityHelper discardHelper;

    public ExtendSANStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    public ExtendSANStorageDomainCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        executeInNewTransaction(() -> {
            setStorageDomainStatus(StorageDomainStatus.Locked, getCompensationContext());
            getCompensationContext().stateChanged();
            return null;
        });
        runVdsCommand(VDSCommandType.ExtendStorageDomain,
                new ExtendStorageDomainVDSCommandParameters(getStoragePoolId(), getStorageDomain()
                        .getId(), getParameters().getLunIds(), getParameters().isForce()));
        updateLunsList();
        executeInNewTransaction(() -> {
            for (LUNs lun : getParameters().getLunsList()) {
                lunHelper.proceedLUNInDb(lun, getStorageDomain().getStorageType(), getStorageDomain().getStorage());
            }

            setStorageDomainStatus(StorageDomainStatus.Active, null);
            getCompensationContext().cleanupCompensationDataAfterSuccessfulCommand();
            return null;
        });
        setSucceeded(true);
    }

    @SuppressWarnings("unchecked")
    private void updateLunsList() {
        VDS spmVds = getAllRunningVdssInPool().stream()
                .filter(vds -> vds.getSpmStatus() == VdsSpmStatus.SPM).findFirst().orElse(null);
        if (spmVds == null) {
            log.error("Could not update LUNs' information of storage domain with VG ID '{}' in the DB.",
                    getStorageDomain().getStorage());
            return;
        }
        try {
            ArrayList<LUNs> upToDateLuns = (ArrayList<LUNs>) runVdsCommand(VDSCommandType.GetVGInfo,
                    new GetVGInfoVDSCommandParameters(spmVds.getId(),
                            getStorageDomain().getStorage())).getReturnValue();
            getParameters().setLunsList(upToDateLuns);
        } catch (RuntimeException e) {
            log.error("Could not get the information for VG ID '{}'; the LUNs' information will not be updated.",
                    getStorageDomain().getStorage());
            log.debug("Exception", e);
        }
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__EXTEND);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (isLunsAlreadyInUse(getParameters().getLunIds())) {
            return false;
        }

        if (!(checkStorageDomain() && checkStorageDomainStatus(StorageDomainStatus.Active))) {
            return false;
        }

        if (!getStorageDomain().getStorageType().isBlockDomain()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }

        final ActionReturnValue returnValue = connectAllHostsToLun();
        if (!returnValue.getSucceeded()) {
            addValidationMessage(EngineMessage.ERROR_CANNOT_EXTEND_CONNECTION_FAILED);

            ConnectAllHostsToLunResult result = returnValue.getActionReturnValue();
            if (result.getFailedVds() != null) {
                addValidationMessage(String.format("$hostName %1s", result.getFailedVds().getName()));
            }

            String lunId = result.getFailedLun() != null ? result.getFailedLun().getLUNId() : "";
            addValidationMessage(String.format("$lun %1s", lunId));
            return false;
        } else {
            // use luns list from connect command
            getParameters().setLunsList(returnValue.getActionReturnValue());
        }

        if (!validate(discardHelper.isExistingDiscardFunctionalityPreserved(returnValue.getActionReturnValue(),
                getStorageDomain()))) {
            return false;
        }
        return true;
    }

    protected ActionReturnValue connectAllHostsToLun() {
        return runInternalAction(
                ActionType.ConnectAllHostsToLun,
                new ExtendSANStorageDomainParameters(getParameters().getStorageDomainId(),
                        getParameters().getLunIds()));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_EXTENDED_STORAGE_DOMAIN
                : AuditLogType.USER_EXTENDED_STORAGE_DOMAIN_FAILED;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getStorageDomainId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }
}
