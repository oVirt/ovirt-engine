package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;

public abstract class RefreshHostInfoCommandBase<T extends VdsActionParameters> extends VdsCommand<T> {

    public RefreshHostInfoCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        // don't check status on internal executions
        return validate(hostExists()) && (isInternalExecution() || validate(hostStatusValid()));
    }

    private ValidationResult hostExists() {
        return getVds() == null ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_HOST_NOT_EXIST) : ValidationResult.VALID;
    }

    private ValidationResult hostStatusValid() {
        VDSStatus hostStatus = getVds().getStatus();
        if (hostStatus != VDSStatus.Maintenance && hostStatus != VDSStatus.Up && hostStatus != VDSStatus.NonOperational) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL,
                    EngineMessage.VAR__HOST_STATUS__UP_MAINTENANCE_OR_NON_OPERATIONAL.name());
        }

        return ValidationResult.VALID;
    }
}
