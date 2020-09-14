package org.ovirt.engine.core;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.aaa.filters.FiltersHelper;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.uutils.net.URLBuilder;

public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 9210030009170727847L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> deployedResponse = SsoOAuthServiceUtils.isSsoDeployed();
        if (deployedResponse.containsKey(WelcomeUtils.ERROR_DESCRIPTION)) {
            request.getSession(true).setAttribute(WelcomeUtils.ERROR_DESCRIPTION,
                    deployedResponse.get(WelcomeUtils.ERROR_DESCRIPTION));
            request.getSession(true).setAttribute(WelcomeUtils.ERROR, deployedResponse.get(WelcomeUtils.ERROR));
            response.sendRedirect(EngineLocalConfig.getInstance().getProperty(WelcomeUtils.ENGINE_URI));
        } else {
            response.sendRedirect(
                    new URLBuilder(FiltersHelper.getEngineSsoUrl(request),
                            WelcomeUtils.OAUTH_AUTHORIZE_URI)
                    .addParameter(WelcomeUtils.HTTP_PARAM_CLIENT_ID,
                            EngineLocalConfig.getInstance().getProperty(WelcomeUtils.ENGINE_SSO_CLIENT_ID))
                    .addParameter(WelcomeUtils.HTTP_PARAM_RESPONSE_TYPE, WelcomeUtils.CODE)
                    .addParameter(WelcomeUtils.HTTP_PARAM_REDIRECT_URI, WelcomeUtils.getOauth2CallbackUrl(request))
                    .addParameter(WelcomeUtils.HTTP_PARAM_SCOPE, request.getParameter(WelcomeUtils.SCOPE))
                    .addParameter(WelcomeUtils.HTTP_PARAM_LOCALE, request.getAttribute(WelcomeUtils.LOCALE).toString())
                    .addParameter(WelcomeUtils.HTTP_PARAM_SOURCE_ADDR, request.getRemoteAddr())
                    .build());
        }
    }

}
