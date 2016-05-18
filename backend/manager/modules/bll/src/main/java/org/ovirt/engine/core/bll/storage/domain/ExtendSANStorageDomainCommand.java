package org.ovirt.engine.core.bll.storage.domain;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.ConnectAllHostsToLunCommand.ConnectAllHostsToLunCommandReturnValue;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ExtendStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ExtendSANStorageDomainCommand<T extends ExtendSANStorageDomainParameters> extends
        StorageDomainCommandBase<T> {

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
        executeInNewTransaction(() -> {
            for (LUNs lun : getParameters().getLunsList()) {
                proceedLUNInDb(lun, getStorageDomain().getStorageType(), getStorageDomain().getStorage());
            }

            setStorageDomainStatus(StorageDomainStatus.Active, null);
            getCompensationContext().cleanupCompensationDataAfterSuccessfulCommand();
            return null;
        });
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__EXTEND);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean validate() {
        super.validate();

        if (isLunsAlreadyInUse(getParameters().getLunIds())) {
            return false;
        }

        if (!(checkStorageDomain() && checkStorageDomainStatus(StorageDomainStatus.Active))) {
            return false;
        }

        if (!getStorageDomain().getStorageType().isBlockDomain()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
            return false;
        }

        final ConnectAllHostsToLunCommandReturnValue connectResult =
                (ConnectAllHostsToLunCommandReturnValue) runInternalAction(
                        VdcActionType.ConnectAllHostsToLun,
                        new ExtendSANStorageDomainParameters(getParameters().getStorageDomainId(), getParameters()
                                .getLunIds()));
        if (!connectResult.getSucceeded()) {
            addValidationMessage(EngineMessage.ERROR_CANNOT_EXTEND_CONNECTION_FAILED);
            if (connectResult.getFailedVds() != null) {
                getReturnValue().getValidationMessages().add(String.format("$hostName %1s",
                        connectResult.getFailedVds().getName()));
            }
            String lunId = connectResult.getFailedLun() != null ? connectResult.getFailedLun().getLUNId() : "";
            getReturnValue().getValidationMessages().add(String.format("$lun %1s", lunId));
            return false;
        } else {
            // use luns list from connect command
            getParameters().setLunsList(connectResult.getActionReturnValue());
        }
        return true;
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
