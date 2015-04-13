package org.ovirt.engine.core.bll.pm;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.RestartVdsVmsOperation;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.pm.FenceActionType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

/**
 * Send a Start action to a power control device.
 *
 * This command should be run mutually exclusive from other fence actions to prevent same action or other fence actions
 * to clear the VMs and start them.
 *
 * @see RestartVdsCommand
 */
@NonTransactiveCommandAttribute
public class StartVdsCommand<T extends FenceVdsActionParameters> extends FenceVdsBaseCommand<T> {
    public StartVdsCommand(T parameters) {
        this(parameters, null);
    }

    public StartVdsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = super.canDoAction();
        VDS vds = getVds();
        if (vds != null) {
            VDSStatus vdsStatus = vds.getStatus();
            if (vdsStatus == VDSStatus.Connecting) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VDS_INTERMITENT_CONNECTIVITY);

            } else if (!legalStatusForStartingVds(vdsStatus)) {
                addCanDoActionMessage(VdcBllMessages.VDS_STATUS_NOT_VALID_FOR_START);
                retValue = false;
                log.error("VDS status for vds '{}' '{}' is '{}'", vds.getId(), vds.getName(), vdsStatus);
            }
        }
        return retValue;
    }

    protected boolean legalStatusForStartingVds(VDSStatus status) {
        return status == VDSStatus.Down
                || status == VDSStatus.NonResponsive
                || status == VDSStatus.Reboot
                || status == VDSStatus.Maintenance;
    }

    @Override
    protected void setStatus() {
        if (getParameters().isChangeHostToMaintenanceOnStart()) {
            setStatus(VDSStatus.Maintenance);
        } else {
            setStatus(VDSStatus.NonResponsive);
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__START);
    }

    @Override
    protected void handleError() {
        addCanDoActionMessage(VdcBllMessages.VDS_FENCE_OPERATION_FAILED);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__START);
        log.error("Failed to run StartVdsCommand on host '{}'", getVdsName());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCanDoActionMessage(VdcBllMessages.VDS_FENCE_OPERATION_FAILED);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__START);

        return getSucceeded() ? AuditLogType.USER_VDS_START : AuditLogType.USER_FAILED_VDS_START;
    }

    @Override
    protected void handleSpecificCommandActions() {
        RestartVdsVmsOperation restartVmsOper = new RestartVdsVmsOperation(
                getContext(),
                getVds()
        );
        restartVmsOper.restartVms(getVmList());
    }

    @Override
    protected void freeLock() {
        if (getParameters().getParentCommand() != VdcActionType.RestartVds) {
            super.freeLock();
        }
    }

    @Override
    protected FenceActionType getAction() {
        return FenceActionType.START;
    }

    @Override
    protected String getRequestedAuditEvent() {
        return AuditLogType.USER_VDS_STOP.name();
    }

    @Override
    protected void setup() {
        // Set status immediately to prevent a race (BZ 636950/656224)
        // Skip setting status if action is manual Start and Host was in Maintenance
        if (getVds().getStatus() != VDSStatus.Maintenance) {
            setStatus();
        }
    }

    @Override
    protected void teardown() {
        // TODO Auto-generated method stub

    }
}
