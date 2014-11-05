package org.ovirt.engine.core.aaa.filters;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.aaa.AuthType;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcLoginReturnValueBase;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(LoginFilter.class);

    private boolean loginAsAdmin;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        loginAsAdmin = Boolean.parseBoolean(filterConfig.getInitParameter("login-as-admin"));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        boolean doFilter = false;
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            if (!FiltersHelper.isAuthenticated(req)) {
                String profileName = (String) request.getAttribute(FiltersHelper.Constants.REQUEST_PROFILE_KEY);

                ExtMap authRecord = (ExtMap) request.getAttribute(FiltersHelper.Constants.REQUEST_AUTH_RECORD_KEY);
                if (authRecord != null) {
                    InitialContext context = new InitialContext();
                    try {
                        VdcLoginReturnValueBase returnValue = (VdcLoginReturnValueBase)FiltersHelper.getBackend(context).login(new
                                LoginUserParameters(
                                        profileName,
                                        authRecord,
                                        (String)request.getAttribute(FiltersHelper.Constants.REQUEST_PASSWORD_KEY),
                                        loginAsAdmin ? VdcActionType.LoginAdminUser : VdcActionType.LoginUser,
                                        (AuthType)request.getAttribute(FiltersHelper.Constants.REQUEST_AUTH_TYPE_KEY)
                               )
                        );
                        if (returnValue.getSucceeded()) {
                            request.setAttribute(
                                    SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY,
                                    returnValue.getSessionId()
                                    );
                        }
                    } finally {
                        context.close();
                    }
                }
            }
            doFilter = true;
        } catch (Exception ex) {
            log.error("Error in login to engine ", ex);
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        if (doFilter) {
            chain.doFilter(request, response);
        }

    }

    @Override
    public void destroy() {
    }

}
