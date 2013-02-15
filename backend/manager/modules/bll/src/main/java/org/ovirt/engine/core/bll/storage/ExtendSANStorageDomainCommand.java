package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.storage.ConnectAllHostsToLunCommand.ConnectAllHostsToLunCommandReturnValue;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.ExtendStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ExtendSANStorageDomainCommand<T extends ExtendSANStorageDomainParameters> extends
        StorageDomainCommandBase<T> {

    private static final long serialVersionUID = 5070823228078328883L;

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
        boolean supportForceExtendVG = Config.<Boolean> GetValue(
                ConfigValues.SupportForceExtendVG, getStoragePool().getcompatibility_version().toString());

        runVdsCommand(VDSCommandType.ExtendStorageDomain,
                new ExtendStorageDomainVDSCommandParameters(getStoragePoolId().getValue(), getStorageDomain()
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

    @SuppressWarnings("unchecked")
    @Override
    protected boolean canDoAction() {
        super.canDoAction();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__EXTEND);
        boolean returnValue = checkStorageDomain() && checkStorageDomainStatus(StorageDomainStatus.Active);
        if (returnValue
                && (getStorageDomain().getStorageType() == StorageType.NFS || getStorageDomain().getStorageType() == StorageType.UNKNOWN)) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
            returnValue = false;
        } else {
            final ConnectAllHostsToLunCommandReturnValue connectResult =
                    (ConnectAllHostsToLunCommandReturnValue) Backend.getInstance().runInternalAction(
                            VdcActionType.ConnectAllHostsToLun,
                            new ExtendSANStorageDomainParameters(getParameters().getStorageDomainId(), getParameters()
                                    .getLunIds()));
            if (!connectResult.getSucceeded()) {
                addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_EXTEND_CONNECTION_FAILED);
                if (connectResult.getFailedVds() != null) {
                    getReturnValue().getCanDoActionMessages().add(String.format("$hostName %1s",
                            connectResult.getFailedVds().getVdsName()));
                }
                String lunId = connectResult.getFailedLun() != null ? connectResult.getFailedLun().getLUN_id() : "";
                getReturnValue().getCanDoActionMessages().add(String.format("$lun %1s", lunId));
                returnValue = false;
            } else {
                // use luns list from connect command
                getParameters().setLunsList((ArrayList<LUNs>) connectResult.getActionReturnValue());
            }
        }
        return returnValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_EXTENDED_STORAGE_DOMAIN
                : AuditLogType.USER_EXTENDED_STORAGE_DOMAIN_FAILED;
    }
}
