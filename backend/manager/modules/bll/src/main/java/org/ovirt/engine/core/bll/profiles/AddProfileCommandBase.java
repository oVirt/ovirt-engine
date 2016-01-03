package org.ovirt.engine.core.bll.profiles;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ProfileParametersBase;
import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public abstract class AddProfileCommandBase<T extends ProfileParametersBase<P>, P extends ProfileBase, Q extends ProfileValidator<P>> extends ProfileCommandBase<T, P> {

    public AddProfileCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        Q validator = getProfileValidator();
        return validate(validator.profileIsSet())
                && validate(validator.parentEntityExists())
                && validate(validator.qosExistsOrNull())
                && validate(validator.profileNameNotUsed());
    }

    @Override
    protected void executeCommand() {
        getParameters().getProfile().setId(Guid.newGuid());
        getProfileDao().save(getParameters().getProfile());
        getReturnValue().setActionReturnValue(getParameters().getProfile().getId());

        if(getParameters().isAddPermissions()) {
            addPermissions();
        }

        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
    }

    protected abstract void addPermissions();

    protected abstract Q getProfileValidator();
}
