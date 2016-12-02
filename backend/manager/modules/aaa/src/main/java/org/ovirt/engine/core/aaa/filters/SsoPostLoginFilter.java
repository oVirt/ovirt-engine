package org.ovirt.engine.core.aaa.filters;

import java.io.IOException;
import java.util.HashMap;
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

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;

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
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    protected Object runQuery(VdcQueryType queryType, String sessionId, InitialContext ctx) {
        VdcQueryParametersBase queryParams = new VdcQueryParametersBase();
        queryParams.setSessionId(sessionId);
        queryParams.setFiltered(FILTER_QUERIES);
        VdcQueryReturnValue result = FiltersHelper.getBackend(ctx).runQuery(queryType, queryParams);
        return result != null && result.getSucceeded() ? result.getReturnValue() : null;
    }

    private Map getUserInfoObject(DbUser loggedInUser, String ssoToken) {
        Map<String, String> obj = new HashMap<>();
        obj.put("userName", loggedInUser.getLoginName()); //$NON-NLS-1$
        obj.put("domain", loggedInUser.getDomain()); //$NON-NLS-1$
        obj.put("isAdmin", Boolean.toString(loggedInUser.isAdmin())); //$NON-NLS-1$
        obj.put("ssoToken", ssoToken); //$NON-NLS-1$
        return obj;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
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
                    String ssoToken = (String) runQuery(VdcQueryType.GetEngineSessionIdToken, engineSessionId, ctx);

                    Object loggedInUser = runQuery(VdcQueryType.GetUserBySessionId, engineSessionId, ctx);
                    if (loggedInUser != null) {
                        log.debug("Adding userInfo to session");
                        req.getSession(true).setAttribute(ATTR_USER_INFO,
                                getUserInfoObject((DbUser) loggedInUser, ssoToken));
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
