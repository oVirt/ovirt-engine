package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.LoginUserParameters;

@NonTransactiveCommandAttribute
public class LoginUserCommand<T extends LoginUserParameters> extends LoginBaseCommand<T> {
    public LoginUserCommand(T parameters) {
        super(parameters);
    }
}
