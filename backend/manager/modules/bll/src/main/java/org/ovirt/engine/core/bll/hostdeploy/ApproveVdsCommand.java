package org.ovirt.engine.core.bll.hostdeploy;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.hostdeploy.ApproveVdsParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;

@NonTransactiveCommandAttribute
public class ApproveVdsCommand<T extends ApproveVdsParameters> extends InstallVdsInternalCommand<T> {

    public ApproveVdsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties;
    }

    @Override
    protected boolean validate() {
        boolean returnValue = true;
        if (getVds() == null) {
            addValidationMessage(EngineMessage.VDS_APPROVE_VDS_NOT_FOUND);
            returnValue = false;
        } else if (getVds().getStatus() != VDSStatus.PendingApproval
                && getVds().getStatus() != VDSStatus.InstallFailed
                && getVds().getStatus() != VDSStatus.InstallingOS) {
            addValidationMessage(EngineMessage.VDS_APPROVE_VDS_IN_WRONG_STATUS);
            returnValue = false;
        }
        return returnValue ? super.validate() : false;
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
            if (getParameters().getActivateHost()) {
                setVdsStatus(VDSStatus.Unassigned);
            } else {
                setVdsStatus(VDSStatus.Maintenance);
            }
        } else if (getParameters().isApprovedByRegister()) {
            // In case of Approval of oVirt host process, the status of the host is re-initialized to PendingApproval
            setVdsStatus(VDSStatus.PendingApproval);
        }
    }
}
