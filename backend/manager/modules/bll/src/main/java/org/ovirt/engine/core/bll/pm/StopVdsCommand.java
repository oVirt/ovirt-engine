package org.ovirt.engine.core.bll.pm;

import static org.ovirt.engine.core.common.errors.EngineMessage.VAR__ACTION__STOP;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.RestartVdsVmsOperation;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.SpmStopVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVdsVMsClearedVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Send a Stop action to a power control device.
 *
 * This command should be run mutually exclusive from other fence actions to prevent same action or other fence actions
 * to clear the VMs and start them.
 *
 * @see RestartVdsCommand
 * @see FenceVdsBaseCommand#restartVdsVms()
 */
@NonTransactiveCommandAttribute
public class StopVdsCommand<T extends FenceVdsActionParameters> extends FenceVdsBaseCommand<T> {
    private static final Logger log = LoggerFactory.getLogger(StopVdsCommand.class);

    @Inject
    private VdsDao vdsDao;
    @Inject
    private VdsDynamicDao vdsDynamicDao;

    public StopVdsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected boolean validate() {
        boolean retValue = true;
        if (!isExecutedAsChildCommand()) {
            retValue = super.validate();
            if (getVds() != null && getVds().getStatus() != VDSStatus.Maintenance) {
                addValidationMessage(EngineMessage.VDS_STATUS_NOT_VALID_FOR_STOP);
                retValue = false;
            }
        }
        getReturnValue().setValid(retValue);
        return retValue;
    }

    @Override
    protected void setStatus() {

        VDSStatus newStatus = VDSStatus.Down;
        if (getParameters().getParentCommand() == ActionType.RestartVds) {
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
        addValidationMessage(VAR__ACTION__STOP);
    }

    @Override
    protected void handleError() {
        addValidationMessage(EngineMessage.VDS_FENCE_OPERATION_FAILED);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST);
        addValidationMessage(EngineMessage.VAR__ACTION__STOP);
        log.error("Failed to run StopVdsCommand on vds '{}'", getVdsName());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_VDS_STOP : AuditLogType.USER_FAILED_VDS_STOP;
    }

    @Override
    protected void handleSpecificCommandActions() {
        List<VM> vmList = getVmList();
        if (vmList.size() > 0) {
            RestartVdsVmsOperation restartVmsOper = new RestartVdsVmsOperation(
                    getContext(),
                    getVds()
            );
            restartVmsOper.restartVms(vmList);
            runVdsCommand(VDSCommandType.UpdateVdsVMsCleared,
                    new UpdateVdsVMsClearedVDSCommandParameters(getVds().getId()));
        }
    }

    @Override
    protected void freeLock() {
        if (getParameters().getParentCommand() != ActionType.RestartVds) {
            super.freeLock();
        }
    }

    @Override
    protected FenceActionType getAction() {
        return FenceActionType.STOP;
    }

    @Override
    protected String getRequestedAuditEvent() {
        return AuditLogType.USER_VDS_START.name();
    }

    @Override
    protected void setup() {
        // Set status immediately to prevent a race (BZ 636950/656224)
        setStatus();

        stopSpm();
    }

    private void stopSpm() {
        // get the host spm status again from the database in order to test it's current state.
        VdsSpmStatus spmStatus = vdsDao.get(getVds().getId()).getSpmStatus();
        // try to stop SPM if action is Restart or Stop and the vds is SPM
        if (spmStatus != VdsSpmStatus.None) {
            runVdsCommand(
                    VDSCommandType.SpmStop,
                    new SpmStopVDSCommandParameters(getVds().getId(), getVds().getStoragePoolId()));
        }
    }

    @Override
    protected void teardown() {
        // Successful fencing with reboot or shutdown op. Clear the power management policy flag
        if (!getParameters().getKeepPolicyPMEnabled()) {
            getVds().setPowerManagementControlledByPolicy(false);
            vdsDynamicDao.updateVdsDynamicPowerManagementPolicyFlag(
                    getVdsId(),
                    getVds().getDynamicData().isPowerManagementControlledByPolicy());
        }
    }
}
