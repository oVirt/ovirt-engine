package org.ovirt.engine.core.bll.storage.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.storage.ConnectAllHostsToLunCommand.ConnectAllHostsToLunCommandReturnValue;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ExtendStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ExtendSANStorageDomainCommand<T extends ExtendSANStorageDomainParameters> extends
        StorageDomainCommandBase<T> {

    protected ExtendSANStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    public ExtendSANStorageDomainCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        executeInNewTransaction(new TransactionMethod<Void>() {
            public Void runInTransaction() {
                setStorageDomainStatus(StorageDomainStatus.Locked, getCompensationContext());
                getCompensationContext().stateChanged();
                return null;
            }
        });
        boolean supportForceExtendVG = Config.<Boolean> getValue(
                ConfigValues.SupportForceExtendVG, getStoragePool().getCompatibilityVersion().toString());

        runVdsCommand(VDSCommandType.ExtendStorageDomain,
                new ExtendStorageDomainVDSCommandParameters(getStoragePoolId(), getStorageDomain()
                        .getId(), getParameters().getLunIds(), getParameters().isForce(), supportForceExtendVG));
        executeInNewTransaction(new TransactionMethod<Void>() {
            public Void runInTransaction() {
                for (LUNs lun : getParameters().getLunsList()) {
                    proceedLUNInDb(lun, getStorageDomain().getStorageType(), getStorageDomain().getStorage());
                }

                setStorageDomainStatus(StorageDomainStatus.Active, null);
                getCompensationContext().resetCompensation();
                return null;
            }
        });
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(EngineMessage.VAR__ACTION__EXTEND);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean canDoAction() {
        super.canDoAction();

        if (isLunsAlreadyInUse(getParameters().getLunIds())) {
            return false;
        }

        if (!(checkStorageDomain() && checkStorageDomainStatus(StorageDomainStatus.Active))) {
            return false;
        }

        if (!getStorageDomain().getStorageType().isBlockDomain()) {
            addCanDoActionMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
            return false;
        }

        final ConnectAllHostsToLunCommandReturnValue connectResult =
                (ConnectAllHostsToLunCommandReturnValue) runInternalAction(
                        VdcActionType.ConnectAllHostsToLun,
                        new ExtendSANStorageDomainParameters(getParameters().getStorageDomainId(), getParameters()
                                .getLunIds()));
        if (!connectResult.getSucceeded()) {
            addCanDoActionMessage(EngineMessage.ERROR_CANNOT_EXTEND_CONNECTION_FAILED);
            if (connectResult.getFailedVds() != null) {
                getReturnValue().getCanDoActionMessages().add(String.format("$hostName %1s",
                        connectResult.getFailedVds().getName()));
            }
            String lunId = connectResult.getFailedLun() != null ? connectResult.getFailedLun().getLUN_id() : "";
            getReturnValue().getCanDoActionMessages().add(String.format("$lun %1s", lunId));
            return false;
        } else {
            // use luns list from connect command
            getParameters().setLunsList((ArrayList<LUNs>) connectResult.getActionReturnValue());
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
