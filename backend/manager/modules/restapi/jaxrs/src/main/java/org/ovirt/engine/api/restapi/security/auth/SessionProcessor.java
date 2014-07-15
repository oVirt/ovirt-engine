package org.ovirt.engine.api.restapi.security.auth;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.Precedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.ovirt.engine.api.common.invocation.Current;
import org.ovirt.engine.api.common.invocation.MetaData;
import org.ovirt.engine.api.restapi.util.SessionHelper;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.GetValueBySessionQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

@Provider
@ServerInterceptor
@Precedence("SECURITY")
public class SessionProcessor implements PreProcessInterceptor, PostProcessInterceptor {

    private Current current;
    private SessionHelper sessionHelper;
    private BackendLocal backend;
    private HttpRequest request;

    public void setBackend(BackendLocal backend) {
        this.backend = backend;
    }

    public void setSessionHelper(SessionHelper sessionHelper) {
        this.sessionHelper = sessionHelper;
    }

    public void setCurrent(Current current) {
        this.current = current;
    }

    public Current getCurrent() {
        return current;
    }


    @Override
    public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure,
            WebApplicationException {
        String engineSessionId = (String) request.getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
        if (engineSessionId == null) {
            throw new Failure("Engine session missing");
        }
        this.request = request;

        sessionHelper.setSessionId(engineSessionId);
        current.set(ApplicationMode.from((Integer) backend.runPublicQuery(VdcQueryType.GetConfigurationValue,
                new GetConfigurationValueParameters(ConfigurationValues.ApplicationMode,
                        ConfigCommon.defaultConfigurationVersion)).getReturnValue()));
        current.set(backend.runPublicQuery(
                VdcQueryType.GetValueBySession,
                new GetValueBySessionQueryParameters(engineSessionId, "user")
                ).getReturnValue());
        return null;
    }


    @Override
    public void postProcess(ServerResponse response) {
        if (current.get(MetaData.class).hasKey("async") &&
                Boolean.TRUE.equals((Boolean) current.get(MetaData.class).get("async"))) {
            request.setAttribute(SessionConstants.REQUEST_ASYNC_KEY, true);
        }
        sessionHelper.clean();
    }

}
