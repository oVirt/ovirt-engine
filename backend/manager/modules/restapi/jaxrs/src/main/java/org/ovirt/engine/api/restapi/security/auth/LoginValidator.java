package org.ovirt.engine.api.restapi.security.auth;

import java.util.List;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.Precedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;

import org.ovirt.engine.api.common.security.auth.Validator;
import org.ovirt.engine.api.common.security.auth.Principal;
import org.ovirt.engine.api.common.invocation.Current;
import org.ovirt.engine.api.common.invocation.MetaData;

import org.ovirt.engine.api.restapi.util.SessionHelper;

import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.LogoutUserParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

@Provider
@ServerInterceptor
@Precedence("SECURITY")
public class LoginValidator implements Validator, PostProcessInterceptor {

    protected static final LogCompat LOG = LogFactoryCompat.getLog(LoginValidator.class);
    // do not log passwords
    protected static final String LOGIN_SUCCESS = "Login success, user: {0} domain: {1}";
    protected static final String LOGIN_FAILURE = "Login failure, user: {0} domain: {1} reason: {2}";
    protected static final String NO_DOMAIN = "Missing domain component in User Principal Name (UPN)";

    private BackendLocal backend;
    private Current current;
    private SessionHelper sessionHelper;

    public void setBackend(BackendLocal backend) {
        this.backend = backend;
    }

    public void setCurrent(Current current) {
        this.current = current;
    }

    public void setSessionHelper(SessionHelper sessionHelper) {
        this.sessionHelper = sessionHelper;
    }

    @Override
    public boolean validate(Principal principal) {
        if (principal.getDomain() == null) {
            return loginFailure(principal, NO_DOMAIN);
        }

        LoginUserParameters params = new LoginUserParameters(principal.getUser(),
                                                             principal.getSecret(),
                                                             principal.getDomain(),
                                                             null, null, null);
        params.setActionType(VdcActionType.LoginAdminUser);

        VdcReturnValueBase ret = backend.Login(sessionHelper.sessionize(params, principal));

        if (ret.getCanDoAction() && ret.getSucceeded()) {
            return loginSuccess(principal, ret);
        } else {
            return loginFailure(principal, reasons(ret));
        }
    }

    private boolean loginSuccess(Principal principal, VdcReturnValueBase ret) {
        LOG.debugFormat(LOGIN_SUCCESS, principal.getUser(), principal.getDomain());
        // cache VdcUser in Current so that it will be available
        // for logoff action on postProcess() traversal
        current.set(ret.getActionReturnValue());
        return true;
    }

    private boolean loginFailure(Principal principal, List<String> reasons) {
        return loginFailure(principal, reasons != null ? reasons.toString() : null);
    }

    private boolean loginFailure(Principal principal, String reason) {
        LOG.infoFormat(LOGIN_FAILURE, principal.getUser(), principal.getDomain(), reason);
        return false;
    }

    private List<String> reasons(VdcReturnValueBase ret) {
        return ret.getCanDoAction() ? ret.getExecuteFailedMessages() : ret.getCanDoActionMessages();
    }

    @Override
    public void postProcess(ServerResponse response) {
        if (!current.get(MetaData.class).hasKey("async") ||
                ((Boolean)current.get(MetaData.class).get("async")) != Boolean.TRUE) {
            VdcUser user = current.get(VdcUser.class);
            if (user != null) {
                backend.Logoff(
                    sessionHelper.sessionize(new LogoutUserParameters(user.getUserId())));
            }
            sessionHelper.clean();
        }
    }
}
