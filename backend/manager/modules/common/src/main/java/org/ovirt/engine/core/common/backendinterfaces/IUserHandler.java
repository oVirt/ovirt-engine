package org.ovirt.engine.core.common.backendinterfaces;

import org.ovirt.engine.core.common.action.*;

public interface IUserHandler {
    VdcReturnValueBase Login(LoginUserParameters parameters);

    VdcReturnValueBase Logoff(LogoutUserParameters parameters);
}
