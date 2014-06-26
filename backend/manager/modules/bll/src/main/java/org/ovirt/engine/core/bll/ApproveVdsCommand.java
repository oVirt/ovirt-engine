package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ApproveVdsParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@NonTransactiveCommandAttribute
public class ApproveVdsCommand<T extends ApproveVdsParameters> extends InstallVdsInternalCommand<T> {

    public ApproveVdsCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        if (getVds() == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_APPROVE_VDS_NOT_FOUND);
            returnValue = false;
        } else if (getVds().getStatus() != VDSStatus.PendingApproval
                && getVds().getStatus() != VDSStatus.InstallFailed
                && getVds().getStatus() != VDSStatus.InstallingOS) {
            addCanDoActionMessage(VdcBllMessages.VDS_APPROVE_VDS_IN_WRONG_STATUS);
            returnValue = false;
        }
        return returnValue ? super.canDoAction() : false;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.VDS_APPROVE;
        } else {
            return super.getAuditLogTypeValue();
        }
    }

    @Override
    protected void executeCommand() {
        if (Config.<Boolean> getValue(ConfigValues.AutoInstallCertificateOnApprove)) {
            super.executeCommand();
        } else {
            setSucceeded(true);
        }
        if (getSucceeded()) {
            runVdsCommand(VDSCommandType.SetVdsStatus,
                            new SetVdsStatusVDSCommandParameters(getVds().getId(), VDSStatus.Unassigned));
        } else if (getParameters().isApprovedByRegister()) {
            // In case of Approval of oVirt host process, the status of the host is re-initialized to PendingApproval
            runVdsCommand(VDSCommandType.SetVdsStatus,
                            new SetVdsStatusVDSCommandParameters(getVds().getId(), VDSStatus.PendingApproval));
        }
    }
}
