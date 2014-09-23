package org.ovirt.engine.core.bll.profiles;

import org.ovirt.engine.core.common.action.ProfileParametersBase;
import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public abstract class RemoveProfileCommandBase<T extends ProfileParametersBase<P>, P extends ProfileBase, Q extends ProfileValidator<P>> extends ProfileCommandBase<T, P> {

    public RemoveProfileCommandBase(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        Q validator = getProfileValidator();
        return validate(validator.profileIsSet())
                && validate(validator.profileExists())
                && validate(validator.isLastProfileInParentEntity());
    }

    @Override
    protected void executeCommand() {
        getProfileDao().remove(getParameters().getProfileId());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }

    protected abstract Q getProfileValidator();
}
