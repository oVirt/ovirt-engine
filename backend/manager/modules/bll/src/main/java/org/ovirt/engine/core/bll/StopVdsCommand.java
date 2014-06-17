package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.errors.VdcBllMessages.VAR__ACTION__STOP;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.UpdateVdsVMsClearedVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * Send a Stop action to a power control device.
 *
 * This command should be run mutually exclusive from other fence actions to prevent same action or other fence actions
 * to clear the VMs and start them.
 *
 * @see RestartVdsCommand
 * @see FenceVdsBaseCommand#restartVdsVms()
 */
@LockIdNameAttribute
@NonTransactiveCommandAttribute
public class StopVdsCommand<T extends FenceVdsActionParameters> extends FenceVdsBaseCommand<T> {
    public StopVdsCommand(T parameters) {
        this(parameters, null);
    }

    public StopVdsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (getParameters().getParentCommand() == VdcActionType.Unknown) {
            retValue = super.canDoAction();
            if (getVds() != null && getVds().getStatus() != VDSStatus.Maintenance) {
                addCanDoActionMessage(VdcBllMessages.VDS_STATUS_NOT_VALID_FOR_STOP);
                retValue = false;
            }
        }
        return retValue;
    }

    @Override
    protected void setStatus() {

        VDSStatus newStatus = VDSStatus.Down;
        if (getParameters().getParentCommand() == VdcActionType.RestartVds) {
            // In case the stop was issued as a result of VDS command , we
            // cannot set the VDS to down -
            // According to bug fix #605215 it can be that backend will crash
            // during restart, and upon restart, all down VDS are not
            // monitored. Instead, we will set the status to rebooting

            newStatus = VDSStatus.Reboot;
        }
        setStatus(newStatus);

    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VAR__ACTION__STOP);
    }

    @Override
    protected void handleError() {
        addCanDoActionMessage(VdcBllMessages.VDS_FENCE_OPERATION_FAILED);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__STOP);
        log.errorFormat("Failed to run StopVdsCommand on vds :{0}", getVdsName());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_VDS_STOP : AuditLogType.USER_FAILED_VDS_STOP;
    }

    @Override
    protected void handleSpecificCommandActions() {
        if (mVmList.size() > 0) {
            RestartVdsVmsOperation restartVmsOper = new RestartVdsVmsOperation(
                    getContext(),
                    getVds()
            );
            restartVmsOper.restartVms(mVmList);
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.UpdateVdsVMsCleared,
                            new UpdateVdsVMsClearedVDSCommandParameters(getVds().getId()));
        }
    }

    private static final Log log = LogFactory.getLog(StopVdsCommand.class);

    @Override
    protected int getRerties() {
        return Config.<Integer> getValue(ConfigValues.FenceStopStatusRetries);
    }

    @Override
    protected int getDelayInSeconds() {
        return Config.<Integer> getValue(ConfigValues.FenceStopStatusDelayBetweenRetriesInSec);
    }

    @Override
    protected void freeLock() {
        if (getParameters().getParentCommand() != VdcActionType.RestartVds) {
            super.freeLock();
        }
    }
}
