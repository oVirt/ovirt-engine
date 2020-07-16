package org.ovirt.engine.core.bll;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.SetHaMaintenanceParameters;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.SetHaMaintenanceModeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

@NonTransactiveCommandAttribute
public class SetHaMaintenanceCommand extends VdsCommand<SetHaMaintenanceParameters> {

    @Inject
    private VmDao vmDao;

    public SetHaMaintenanceCommand(SetHaMaintenanceParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        if (getVdsId().equals(Guid.Empty)) {
            List<OriginType> hostedEngineOriginTypes = Arrays.asList(
                    OriginType.HOSTED_ENGINE,
                    OriginType.MANAGED_HOSTED_ENGINE);
            List<VM> hostedEngineVms = vmDao.getVmsByOrigins(hostedEngineOriginTypes);
            setVdsId(hostedEngineVms.stream()
                    .map(VM::getRunOnVds)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null));
            log.info("setting host to the one the hosted engine VM runs on: {}", getVdsId());
        }
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
        } catch (EngineException e) {
            log.error("Could not {} {} Hosted Engine HA maintenance mode on host '{}'",
                    params.getIsEnabled() ? "enable" : "disable",
                    params.getMode().name().toLowerCase(),
                    getVdsName());
        }
        getReturnValue().setSucceeded(succeeded);
    }

    @Override
    protected boolean validate() {
        HostValidator hostValidator = HostValidator.createInstance(getVds());

        if (!validate(hostValidator.hostExists())
                || !validate(hostValidator.isUp())) {
            return false;
        }
        if (!getVds().getHighlyAvailableIsConfigured()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VDS_HA_NOT_CONFIGURED);
        }
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getVdsId(),
                VdcObjectType.VDS, getActionType().getActionGroup()));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_SET_HOSTED_ENGINE_MAINTENANCE
                : AuditLogType.USER_FAILED_TO_SET_HOSTED_ENGINE_MAINTENANCE;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__VM__CLUSTER);
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }
}
