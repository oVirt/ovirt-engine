package org.ovirt.engine.core.aaa.filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.naming.InitialContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.aaa.SSOOAuthServiceUtils;
import org.ovirt.engine.core.aaa.SSOUtils;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.ovirt.engine.core.common.queries.GetEngineSessionIdForSSOTokenQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSORestApiAuthFilter implements Filter {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String scope = "ovirt-app-api";
    private static final String BASIC = "Basic";
    private static final String BEARER = "Bearer";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // empty
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.debug("Entered SSORestApiAuthFilter");
        HttpServletRequest req = (HttpServletRequest) request;
        if (!FiltersHelper.isAuthenticated(req) || !FiltersHelper.isSessionValid((HttpServletRequest) request)) {
            log.debug("SSORestApiAuthFilter authenticating with sso");
            authenticateWithSSO(req, (HttpServletResponse) response);
        }
        chain.doFilter(request, response);
    }

    protected void authenticateWithSSO(HttpServletRequest req, HttpServletResponse res) throws ServletException {
        String headerValue = req.getHeader(FiltersHelper.Constants.HEADER_AUTHORIZATION);
        if (headerValue != null && (headerValue.startsWith(BASIC) || headerValue.startsWith(BEARER))) {
            try {
                String token;
                boolean userSessionExists = false;
                if (headerValue.startsWith(BASIC)) {
                    log.debug("SSORestApiAuthFilter authenticating using BASIC header");
                    Map<String, Object> response = SSOOAuthServiceUtils.authenticate(req, scope);
                    FiltersHelper.isStatusOk(response);
                    token = (String) response.get("access_token");
                    log.debug("SSORestApiAuthFilter successfully authenticated using BASIC header");
                } else if (headerValue.startsWith(BEARER)) {
                    log.debug("SSORestApiAuthFilter authenticating using BEARER header");
                    token = headerValue.substring("Bearer".length()).trim();
                    InitialContext ctx = new InitialContext();
                    try {
                        VdcQueryReturnValue queryRetVal = FiltersHelper.getBackend(ctx).runQuery(
                                VdcQueryType.GetEngineSessionIdForSSOToken,
                                new GetEngineSessionIdForSSOTokenQueryParameters(token));
                        if (queryRetVal.getSucceeded()) {
                            log.debug("SSORestApiAuthFilter successfully authenticated using BEARER header");
                            req.getSession(true).setAttribute(
                                    SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY,
                                    queryRetVal.getReturnValue());
                            req.getSession(true).setAttribute(
                                    FiltersHelper.Constants.REQUEST_LOGIN_FILTER_AUTHENTICATION_DONE,
                                    true);
                            userSessionExists = true;
                        }
                    } finally {
                        ctx.close();
                    }
                } else {
                    throw new RuntimeException(String.format("Unsupported authentication header: %s", headerValue));
                }

                if (!userSessionExists) {
                    Map<String, Object> payload = FiltersHelper.getPayloadForToken(token);
                    String scope = (String) payload.get("scope");
                    if (StringUtils.isEmpty(scope) ||
                            !Arrays.asList(scope.trim().split("\\s *")).contains("ovirt-app-api")) {
                        throw new RuntimeException("The required scope ovirt-app-api is not granted.");
                    }
                    SSOUtils.createUserSession(req, payload, false);
                }
                req.setAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY,
                        req.getSession().getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY));
                req.setAttribute(FiltersHelper.Constants.REQUEST_LOGIN_FILTER_AUTHENTICATION_DONE,
                        req.getSession().getAttribute(FiltersHelper.Constants.REQUEST_LOGIN_FILTER_AUTHENTICATION_DONE));
            } catch (Exception e) {
                log.error("Cannot authenticate using authentication Headers: {}", e.getMessage());
                log.debug("Cannot authenticate using authentication Headers", e);
            }
        }
    }

    @Override
    public void destroy() {
        // empty
    }
}
