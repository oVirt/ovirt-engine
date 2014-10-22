package org.ovirt.engine.core.bll;


import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.SetHaMaintenanceParameters;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.SetHaMaintenanceModeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Version;

@NonTransactiveCommandAttribute
public class SetHaMaintenanceCommand extends VdsCommand<SetHaMaintenanceParameters> {

    public SetHaMaintenanceCommand(SetHaMaintenanceParameters vdsActionParameters) {
        super(vdsActionParameters);
    }

    @Override
    protected void executeCommand() {
        boolean succeeded = false;
        SetHaMaintenanceParameters params = getParameters();
        try {
            succeeded = runVdsCommand(
                    VDSCommandType.SetHaMaintenanceMode,
                    new SetHaMaintenanceModeVDSCommandParameters(
                            getVds(), params.getMode(), params.getIsEnabled()))
                    .getSucceeded();
        } catch (VdcBLLException e) {
            log.error("Could not {} {} Hosted Engine HA maintenance mode on host '{}'",
                    params.getIsEnabled() ? "enable" : "disable",
                    params.getMode().name().toLowerCase(),
                    getVdsName());
        }
        getReturnValue().setSucceeded(succeeded);
    }

    @Override
    protected boolean canDoAction() {
        VdsValidator vdsValidator = new VdsValidator(getVds());

        if (!validate(vdsValidator.exists())
                || !validate(vdsValidator.isUp())) {
            return false;
        }
        if (getVds().getVdsGroupCompatibilityVersion().compareTo(Version.v3_4) < 0) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VDS_HA_MAINT_NOT_SUPPORTED);
        }
        if (!getVds().getHighlyAvailableIsConfigured()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VDS_HA_NOT_CONFIGURED);
        }
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getVdsId(),
                VdcObjectType.VDS, getActionType().getActionGroup()));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_SET_HOSTED_ENGINE_MAINTENANCE
                : AuditLogType.USER_FAILED_TO_SET_HOSTED_ENGINE_MAINTENANCE;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM__CLUSTER);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
    }
}
