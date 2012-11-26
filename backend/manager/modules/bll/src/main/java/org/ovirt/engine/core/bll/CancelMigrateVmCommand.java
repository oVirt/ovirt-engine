package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class CancelMigrateVmCommand<T extends VmOperationParameterBase> extends VmCommand<T> {

    private static final long serialVersionUID = -558126793809498909L;

    public CancelMigrateVmCommand(T param) {
        super(param);
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue retVal = Backend.getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.CancelMigrate,
                        new VdsAndVmIDVDSParametersBase(getVm().getRunOnVds().getValue(),
                                getParameters().getVmId()));

        setSucceeded(retVal.getSucceeded());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CANCEL_MIGRATE);
    }

    @Override
    protected boolean canDoAction() {
        if (getVm() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_EXIST);
            return false;
        }
        if (getVm().getStatus() != VMStatus.MigratingFrom) {
            addCanDoActionMessage(VdcBllMessages.VM_CANNOT_CANCEL_MIGRATION_WHEN_VM_IS_NOT_MIGRATING);
            return false;
        }
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.VM_CANCEL_MIGRATION
                : AuditLogType.VM_CANCEL_MIGRATION_FAILED;
    }
}
