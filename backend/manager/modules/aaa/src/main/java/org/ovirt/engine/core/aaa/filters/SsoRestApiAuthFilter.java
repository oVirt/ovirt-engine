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

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.aaa.SsoUtils;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.ovirt.engine.core.common.queries.GetEngineSessionIdForSsoTokenQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoRestApiAuthFilter implements Filter {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String scope = "ovirt-app-api";
    private static final String BASIC = "Basic";
    private static final String BEARER = "Bearer";

    @Override
    public void init(FilterConfig filterConfig) {
        // empty
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.debug("Entered SsoRestApiAuthFilter");
        HttpServletRequest req = (HttpServletRequest) request;
        if (!FiltersHelper.isAuthenticated(req) || !FiltersHelper.isSessionValid((HttpServletRequest) request)) {
            log.debug("SsoRestApiAuthFilter authenticating with sso");
            authenticateWithSso(req);
        }
        chain.doFilter(request, response);
    }

    protected void authenticateWithSso(HttpServletRequest req) {
        String headerValue = req.getHeader(FiltersHelper.Constants.HEADER_AUTHORIZATION);
        if (headerValue != null && (headerValue.startsWith(BASIC) || headerValue.startsWith(BEARER))) {
            try {
                String token;
                boolean userSessionExists = false;
                if (headerValue.startsWith(BASIC)) {
                    log.debug("SsoRestApiAuthFilter authenticating using BASIC header");
                    Map<String, Object> response = SsoOAuthServiceUtils.authenticate(req, scope);
                    FiltersHelper.isStatusOk(response);
                    token = (String) response.get("access_token");
                    log.debug("SsoRestApiAuthFilter successfully authenticated using BASIC header");
                } else if (headerValue.startsWith(BEARER)) {
                    log.debug("SsoRestApiAuthFilter authenticating using BEARER header");
                    token = headerValue.substring("Bearer".length()).trim();
                    InitialContext ctx = new InitialContext();
                    try {
                        QueryReturnValue queryRetVal = FiltersHelper.getBackend(ctx).runPublicQuery(
                                QueryType.GetEngineSessionIdForSsoToken,
                                new GetEngineSessionIdForSsoTokenQueryParameters(token));
                        if (queryRetVal.getSucceeded() && StringUtils.isNotEmpty(queryRetVal.getReturnValue())) {
                            log.debug("SsoRestApiAuthFilter successfully authenticated using BEARER header");
                            req.setAttribute(
                                    SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY,
                                    queryRetVal.getReturnValue());
                            req.setAttribute(
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
                    SsoUtils.createUserSession(req, payload, false);
                }
            } catch (Exception e) {
                req.setAttribute(
                        SessionConstants.SSO_AUTHENTICATION_ERR_MSG,
                        e.getMessage());
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
