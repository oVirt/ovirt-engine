package org.ovirt.engine.core.bll.profiles;

import org.ovirt.engine.core.common.action.ProfileParametersBase;
import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;

public abstract class UpdateProfileCommandBase<T extends ProfileParametersBase<P>, P extends ProfileBase, Q extends ProfileValidator<P>> extends ProfileCommandBase<T, P> {

    public UpdateProfileCommandBase(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
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

    protected abstract Q getProfileValidator();
}
