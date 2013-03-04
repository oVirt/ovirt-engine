package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ApproveVdsParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.VdcBllMessages;

@NonTransactiveCommandAttribute
public class ApproveVdsCommand<T extends ApproveVdsParameters> extends InstallVdsCommand<T> {

    public ApproveVdsCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        if (getVds() == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_APPROVE_VDS_NOT_FOUND);
            returnValue = false;
        } else if (getVds().getVdsType() != VDSType.oVirtNode) {
            addCanDoActionMessage(VdcBllMessages.VDS_APPROVE_WRONG_VDS_TYPE);
            returnValue = false;
        } else if (getVds().getStatus() != VDSStatus.PendingApproval
                && getVds().getStatus() != VDSStatus.InstallFailed) {
            addCanDoActionMessage(VdcBllMessages.VDS_APPROVE_VDS_IN_WRONG_STATUS.toString());
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
        if (Config.<Boolean> GetValue(ConfigValues.AutoInstallCertificateOnApprove)) {
            super.executeCommand();
        } else {
            setSucceeded(true);
        }
        if (getSucceeded()) {
            Backend.getInstance()
            .getResourceManager()
            .RunVdsCommand(VDSCommandType.SetVdsStatus,
                            new SetVdsStatusVDSCommandParameters(getVds().getId(), VDSStatus.Unassigned));
        } else if (getParameters().isApprovedByRegister()) {
            // In case of Approval of oVirt host process, the status of the host is re-initialized to PendingApproval
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.SetVdsStatus,
                            new SetVdsStatusVDSCommandParameters(getVds().getId(), VDSStatus.PendingApproval));
        }
    }
}
