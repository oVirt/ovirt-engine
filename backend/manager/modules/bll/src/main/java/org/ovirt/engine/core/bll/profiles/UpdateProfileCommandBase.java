package org.ovirt.engine.core.bll.profiles;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ProfileParametersBase;
import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;
import org.ovirt.engine.core.common.errors.EngineMessage;

public abstract class UpdateProfileCommandBase<T extends ProfileParametersBase<P>, P extends ProfileBase, Q extends ProfileValidator<P>> extends ProfileCommandBase<T, P> {

    public UpdateProfileCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        Q validator = getProfileValidator();
        return validate(validator.profileIsSet())
                && validate(validator.profileExists())
                && validate(validator.profileNameNotUsed())
                && validate(validator.parentEntityNotChanged())
                && validate(validator.qosExistsOrNull());
    }

    @Override
    protected void executeCommand() {
        getProfileDao().update(getParameters().getProfile());
        getReturnValue().setActionReturnValue(getParameters().getProfile().getId());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }

    protected abstract Q getProfileValidator();
}
