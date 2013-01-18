package org.ovirt.engine.api.restapi.security.auth;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.Precedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.ovirt.engine.api.common.invocation.Current;
import org.ovirt.engine.api.common.invocation.MetaData;
import org.ovirt.engine.api.common.security.auth.Principal;
import org.ovirt.engine.api.common.security.auth.SessionUtils;
import org.ovirt.engine.api.common.security.auth.Validator;
import org.ovirt.engine.api.restapi.util.SessionHelper;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.LogoutUserParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

@Provider
@ServerInterceptor
@Precedence("SECURITY")
public class LoginValidator implements Validator, PostProcessInterceptor {

    protected static final Log LOG = LogFactory.getLog(LoginValidator.class);
    // do not log passwords
    protected static final String LOGIN_SUCCESS = "Login success, user: {0} domain: {1}";
    protected static final String LOGIN_FAILURE = "Login failure, user: {0} domain: {1} reason: {2}";
    protected static final String VALIDATE_SESSION_SUCCESS = "Validating session succeeded";
    protected static final String VALIDATE_SESSION_FAILURE = "Validating session failed, reason: {0}";
    protected static final String NO_DOMAIN = "Missing domain component in User Principal Name (UPN)";

    private BackendLocal backend;
    private Current current;
    private SessionHelper sessionHelper;
    private boolean persistentSession = true;

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
    public boolean validate(Principal principal, String sessionId) {
        if (principal.getDomain() == null) {
            return loginFailure(principal, NO_DOMAIN);
        }

        LoginUserParameters params = new LoginUserParameters(principal.getUser(),
                principal.getSecret(),
                principal.getDomain(),
                null, null, null);
        params.setActionType(VdcActionType.LoginUser);
        sessionHelper.setSessionId(sessionId);
        VdcReturnValueBase ret = backend.Login(sessionHelper.sessionize(params));

        if (ret.getCanDoAction() && ret.getSucceeded()) {
            return loginSuccess(principal, ret);
        } else {
            return loginFailure(principal, reasons(ret));
        }
    }

    @Override
    public Principal validate(String sessionId) {
        Principal principal = null;
        VdcQueryParametersBase params = new VdcQueryParametersBase();
        params.setSessionId(sessionId);
        VdcQueryReturnValue queryReturnValue = backend.RunPublicQuery(VdcQueryType.ValidateSession, params);
        if (queryReturnValue != null) {
            VdcUser vdcUser = (VdcUser) queryReturnValue.getReturnValue();
            if (vdcUser != null) {
                principal = new Principal(vdcUser.getUserName(), vdcUser.getPassword(), vdcUser.getDomainControler());
                sessionHelper.setSessionId(sessionId);
                current.set(vdcUser);
            }
        }
        if (principal != null) {
            validateSessionSucceeded(queryReturnValue);
        } else {
            validateSessionFailed(queryReturnValue);
        }
        return principal;
    }

    private boolean loginSuccess(Principal principal, VdcReturnValueBase ret) {
        LOG.debugFormat(LOGIN_SUCCESS, principal.getUser(), principal.getDomain());
        // cache VdcUser in Current so that it will be available
        // for logoff action on postProcess() traversal
        current.set(ret.getActionReturnValue());
        current.set(getApplicationMode());
        return true;
    }

    private ApplicationMode getApplicationMode() {
        VdcQueryReturnValue result = backend.RunPublicQuery(VdcQueryType.GetConfigurationValue,
                new GetConfigurationValueParameters(ConfigurationValues.ApplicationMode, ConfigCommon.defaultConfigurationVersion));
        ApplicationMode appMode = null;
        if (result.getSucceeded())
        {
            appMode = ApplicationMode.from((Integer) result.getReturnValue());
        }
        else
        {
            appMode = ApplicationMode.AllModes;
        }
        return appMode;
    }

    private void validateSessionSucceeded(VdcQueryReturnValue ret) {
        LOG.debugFormat(VALIDATE_SESSION_SUCCESS);
        // cache VdcUser in Current so that it will be available
        // for logoff action on postProcess() traversal
        current.set(ret.getReturnValue());
    }

    private boolean loginFailure(Principal principal, List<String> reasons) {
        return loginFailure(principal, reasons != null ? reasons.toString() : null);
    }

    private boolean loginFailure(Principal principal, String reason) {
        LOG.infoFormat(LOGIN_FAILURE, principal.getUser(), principal.getDomain(), reason);
        return false;
    }

    private void validateSessionFailed(VdcQueryReturnValue ret) {
        LOG.infoFormat(VALIDATE_SESSION_FAILURE, ret.getExceptionString());
    }

    private List<String> reasons(VdcReturnValueBase ret) {
        return ret.getCanDoAction() ? ret.getExecuteFailedMessages() : ret.getCanDoActionMessages();
    }

    @Override
    public void postProcess(ServerResponse response) {
        HttpSession httpSession = getCurrentSession(false);
        if (!current.get(MetaData.class).hasKey("async") ||
                ((Boolean)current.get(MetaData.class).get("async")) != Boolean.TRUE) {
            VdcUser user = current.get(VdcUser.class);
            if (user != null) {
                if (!persistentSession) {
                    backend.Logoff(
                            sessionHelper.sessionize(new LogoutUserParameters(user.getUserId())));
                    if (httpSession != null) {
                        httpSession.invalidate();
                    }
                } else if (httpSession != null && httpSession.isNew()) {
                    response.getMetadata().add(SessionUtils.JSESSIONID_HEADER,
                            httpSession.getId());
                }
            }
        }
        sessionHelper.clean();
    }

    @Override
    public void usePersistentSession(boolean persistentSession) {
        this.persistentSession = persistentSession;
    }

    // Here to ease mocking it in the tester
    protected HttpSession getCurrentSession(boolean create) {
        return SessionUtils.getCurrentSession(create);
    }

}
