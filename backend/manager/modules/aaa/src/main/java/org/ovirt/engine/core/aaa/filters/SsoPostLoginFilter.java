package org.ovirt.engine.core.aaa.filters;

import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For non-GWT applications only (please see SsoPostLoginServlet otherwise).
 *
 * If SSO is configured properly, the SsoPostLoginFilter adds logged user details
 * to the session scope based on the sessionId.
 */
public class SsoPostLoginFilter implements Filter {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String ATTR_USER_INFO = "userInfo";
    private static final boolean FILTER_QUERIES = true;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    protected Object runQuery(QueryType queryType, String sessionId, InitialContext ctx) {
        QueryParametersBase queryParams = new QueryParametersBase();
        queryParams.setSessionId(sessionId);
        queryParams.setFiltered(FILTER_QUERIES);
        QueryReturnValue result = FiltersHelper.getBackend(ctx).runQuery(queryType, queryParams);
        return result != null && result.getSucceeded() ? result.getReturnValue() : null;
    }

    private Map getUserInfoObject(DbUser loggedInUser, String ssoToken, long sessionCreationTimeInMillis) {
        Map<String, String> obj = new HashMap<>();
        obj.put("userName", loggedInUser.getLoginName()); //$NON-NLS-1$
        obj.put("domain", loggedInUser.getDomain()); //$NON-NLS-1$
        obj.put("isAdmin", Boolean.toString(loggedInUser.isAdmin())); //$NON-NLS-1$
        obj.put("ssoToken", ssoToken); //$NON-NLS-1$
        obj.put("userId", loggedInUser.getId().toString()); //$NON-NLS-1$
        // Auxiliary helper attribute used to determine if reload is triggered due to new login or simple browser
        // refresh. Never use it for session validation!
        obj.put("sessionAgeInSec", Long.toString((System.currentTimeMillis() - sessionCreationTimeInMillis) / 1000)); //$NON-NLS-1$
        return obj;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        log.debug("Entered SsoPostLoginFilter");
        HttpServletRequest req = (HttpServletRequest) request;

        try {
            String engineSessionId = (String) req.getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
            if (StringUtils.isEmpty(engineSessionId) && req.getSession(false) != null) {
                engineSessionId = (String) req.getSession(false).
                        getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
            }
            if (StringUtils.isNotEmpty(engineSessionId)) {
                InitialContext ctx = new InitialContext();
                try {
                    String ssoToken = (String) runQuery(QueryType.GetEngineSessionIdToken, engineSessionId, ctx);

                    Object loggedInUser = runQuery(QueryType.GetUserBySessionId, engineSessionId, ctx);
                    if (loggedInUser != null) {
                        log.debug("Adding userInfo to session");
                        req.getSession(true).setAttribute(ATTR_USER_INFO,
                                getUserInfoObject((DbUser) loggedInUser, ssoToken, req.getSession().getCreationTime()));
                    } else {
                        log.info("Failed to find logged user by sessionId");
                    }

                    chain.doFilter(request, response);
                } finally {
                    ctx.close();
                }
            } else {
                log.warn("Missing sessionId in either request or session scope. " +
                        "Please configure SSO properly with SsoPostLoginServlet.");
            }
        } catch (Exception ex) {
            log.error("Unable to get token for engine session {}", ex.getMessage());
            log.debug("Exception", ex);
        }
        log.debug("Exiting SsoPostLoginFilter");
    }

    @Override
    public void destroy() {
    }
}
