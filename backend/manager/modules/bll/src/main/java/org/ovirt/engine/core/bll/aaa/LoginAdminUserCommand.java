package org.ovirt.engine.core.bll.aaa;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

@NonTransactiveCommandAttribute
public class LoginAdminUserCommand<T extends LoginUserParameters> extends LoginUserCommand<T> {
    public LoginAdminUserCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean autheticated = super.canDoAction();

        // only admin users can use LoginAdmin command
        if (autheticated) {
            autheticated = getCurrentUser().isAdmin();
            if (!autheticated) {
                addCanDoActionMessage(VdcBllMessages.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);
                logAutheticationFailure();
            }
        }
        return autheticated;
    }
}
