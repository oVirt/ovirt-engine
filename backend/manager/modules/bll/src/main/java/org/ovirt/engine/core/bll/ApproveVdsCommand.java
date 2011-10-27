package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ApproveVdsParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
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
    protected void executeCommand() {
        ApproveVds(getVds());
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        if (getVds().getvds_type() != VDSType.oVirtNode) {
            addCanDoActionMessage(VdcBllMessages.VDS_APPROVE_WRONG_VDS_TYPE);
            returnValue = false;
        } else {
            if (getVds() == null) {
                addCanDoActionMessage(VdcBllMessages.VDS_APPROVE_VDS_NOT_FOUND);
                returnValue = false;
            } else if (getVds().getstatus() != VDSStatus.PendingApproval
                    && getVds().getstatus() != VDSStatus.InstallFailed) {
                getReturnValue().getCanDoActionMessages()
                .add(VdcBllMessages.VDS_APPROVE_VDS_IN_WRONG_STATUS.toString());
                returnValue = false;
            }
        }
        return returnValue ? super.canDoAction() : false;
    }

    private AuditLogType _failureLogTypeValue = AuditLogType.forValue(0);

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (!getSucceeded()) {
            if (_failureLogTypeValue == AuditLogType.VDS_INSTALL_FAILED) {
                AddCustomValue("FailedInstallMessage", getErrorMessage(_vdsInstaller.getErrorMessage()));
            }
            return _failureLogTypeValue;
        } else {
            return AuditLogType.VDS_APPROVE;
        }
    }

    public void ApproveVds(VDS vds) {

        _failureLogTypeValue = AuditLogType.VDS_INSTALL_FAILED;
        if (Config.<Boolean> GetValue(ConfigValues.PowerClientAutoInstallCertificateOnApprove)) {
            super.executeCommand();
        } else {
            setSucceeded(true);
        }
        if (getSucceeded()) {
            _failureLogTypeValue = AuditLogType.VDS_APPROVE_FAILED;
            Backend.getInstance()
            .getResourceManager()
            .RunVdsCommand(VDSCommandType.SetVdsStatus,
                           new SetVdsStatusVDSCommandParameters(vds.getvds_id(), VDSStatus.Unassigned));
        } else if (getParameters().isApprovedByRegister()) {
            // In case of Approval of oVirt host process, the status of the host is re-initialized to PendingApproval
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.SetVdsStatus,
                            new SetVdsStatusVDSCommandParameters(vds.getvds_id(), VDSStatus.PendingApproval));
        }
    }

}
