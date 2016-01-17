package org.ovirt.engine.core.bll.profiles;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ProfileParametersBase;
import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;
import org.ovirt.engine.core.common.errors.EngineMessage;

public abstract class RemoveProfileCommandBase<T extends ProfileParametersBase<P>, P extends ProfileBase, Q extends ProfileValidator<P>> extends ProfileCommandBase<T, P> {

    public RemoveProfileCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        Q validator = getProfileValidator();
        return validate(validator.profileIsSet())
                && validate(validator.profileExists())
                && validate(validator.isLastProfileInParentEntity())
                && validate(validator.profileNotUsed());
    }

    @Override
    protected void executeCommand() {
        getProfileDao().remove(getParameters().getProfileId());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }

    protected abstract Q getProfileValidator();
}
