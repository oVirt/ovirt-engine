package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class LoginAdminUserCommand<T extends LoginUserParameters> extends LoginUserCommand<T> {

    private static Log log = LogFactory.getLog(LoginAdminUserCommand.class);

    public LoginAdminUserCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean autheticated = isUserCanBeAuthenticated();

        // only admin users can use LoginAdmin command
        if (autheticated) {
            autheticated = MultiLevelAdministrationHandler.isAdminUser(getCurrentUser().getUserId());

            if (!autheticated) {
                addCanDoActionMessage(VdcBllMessages.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);
                if (log.isDebugEnabled()) {
                    log.debugFormat("LoginAdminUser: No admin role found for user - login failed.");
                }
            } else {
                autheticated = persistUserSession();
            }

        }
        return autheticated;
    }
}
