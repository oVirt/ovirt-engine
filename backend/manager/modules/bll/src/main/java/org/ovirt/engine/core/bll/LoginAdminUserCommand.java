package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class LoginAdminUserCommand<T extends LoginUserParameters> extends LoginUserCommand<T> {

    private static LogCompat log = LogFactoryCompat.getLog(LoginAdminUserCommand.class);

    public LoginAdminUserCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        // first call to super to check user with directory services
        // it also added to db in case it is needed
        boolean autheticated = super.canDoAction();

        // only admin users can use LoginAdmin command
        if (autheticated) {
            autheticated = MultiLevelAdministrationHandler.isAdminUser(getCurrentUser().getUserId());

            if (!autheticated) {
                addCanDoActionMessage(VdcBllMessages.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);
                if (log.isDebugEnabled()) {
                    log.debugFormat("LoginAdminUser: No admin role found for user - login failed.");
                }
            }

        }
        return autheticated;
    }
}
