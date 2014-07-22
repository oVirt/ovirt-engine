package org.ovirt.engine.core.bll.aaa;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.action.LoginUserParameters;

@NonTransactiveCommandAttribute
public class LoginUserCommand<T extends LoginUserParameters> extends LoginBaseCommand<T> {
    public LoginUserCommand(T parameters) {
        super(parameters);
    }
}
