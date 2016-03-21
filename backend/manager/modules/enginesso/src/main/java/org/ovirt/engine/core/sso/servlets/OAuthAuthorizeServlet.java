package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.sso.utils.InteractiveAuth;
import org.ovirt.engine.core.sso.utils.OAuthException;
import org.ovirt.engine.core.sso.utils.SSOConstants;
import org.ovirt.engine.core.sso.utils.SSOContext;
import org.ovirt.engine.core.sso.utils.SSOSession;
import org.ovirt.engine.core.sso.utils.SSOUtils;
import org.ovirt.engine.core.uutils.net.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthAuthorizeServlet extends HttpServlet {
    private static final long serialVersionUID = -4822437649213489822L;
    private static Logger log = LoggerFactory.getLogger(OAuthAuthorizeServlet.class);

    private SSOContext ssoContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ssoContext = SSOUtils.getSsoContext(config.getServletContext());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            log.debug("Entered AuthorizeServlet QueryString: {}, Parameters : {}",
                    request.getQueryString(),
                    SSOUtils.getRequestParameters(request));
            String clientId = SSOUtils.getRequestParameter(request, SSOConstants.HTTP_PARAM_CLIENT_ID);
            String responseType = SSOUtils.getRequestParameter(request, SSOConstants.JSON_RESPONSE_TYPE);
            String scope = SSOUtils.getScopeRequestParameter(request, "");
            String state = SSOUtils.getRequestParameter(request, SSOConstants.HTTP_PARAM_STATE, "");
            String redirectUri = SSOUtils.getParameter(request, SSOConstants.HTTP_PARAM_REDIRECT_URI);
            SSOUtils.validateClientRequest(request, clientId, null, scope, redirectUri);

            if (!responseType.equals("code")) {
                throw new OAuthException(SSOConstants.ERR_CODE_INVALID_REQUEST,
                        String.format(
                                ssoContext.getLocalizationUtils().localize(
                                        SSOConstants.APP_ERROR_UNSUPPORTED_PARAMETER_IN_REQUEST,
                                        (Locale) request.getAttribute(SSOConstants.LOCALE)),
                                responseType,
                                SSOConstants.JSON_RESPONSE_TYPE));
            }

            login(request, response, clientId, scope, state, redirectUri);
        } catch (Exception ex) {
            SSOUtils.redirectToErrorPage(request, response, ex);
        }
    }

    private void login(
            HttpServletRequest request,
            HttpServletResponse response,
            String clientId,
            String scope,
            String state,
            String redirectUri) throws Exception {
        log.debug("Entered login queryString: {}", request.getQueryString());
        String redirectUrl;

        // Create the session
        request.getSession(true);

        SSOSession ssoSession = SSOUtils.getSsoSession(request);
        ssoSession.setClientId(clientId);
        ssoSession.setRedirectUri(redirectUri);
        ssoSession.setScope(scope);
        ssoSession.setState(state);
        ssoSession.getHttpSession().setMaxInactiveInterval(-1);

        if (SSOUtils.isUserAuthenticated(request)) {
            log.debug("User is authenticated redirecting to interactive-redirect-to-module");
            redirectUrl = request.getContextPath() + SSOConstants.INTERACTIVE_REDIRECT_TO_MODULE_URI;
        } else if (SSOUtils.scopeAsList(scope).contains("ovirt-ext=auth:identity")) {
            redirectUrl = new URLBuilder(SSOUtils.getRedirectUrl(request))
                    .addParameter("error_code", SSOConstants.ERR_OVIRT_CODE_NOT_AUTHENTICATED)
                    .addParameter("error", SSOConstants.ERR_CODE_NOT_AUTHENTICATED_MSG).build();
        } else {
            ssoSession.setAuthStack(getAuthSeq(scope));
            if (ssoSession.getAuthStack().isEmpty()) {
                throw new OAuthException(SSOConstants.ERR_CODE_ACCESS_DENIED,
                        ssoContext.getLocalizationUtils().localize(
                                SSOConstants.APP_ERROR_NO_VALID_AUTHENTICATION_MECHANISM_FOUND,
                                (Locale) request.getAttribute(SSOConstants.LOCALE)));
            }
            redirectUrl = request.getContextPath() + SSOConstants.INTERACTIVE_LOGIN_NEXT_AUTH_URI;
        }
        log.debug("Redirecting to url: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    private Stack<InteractiveAuth> getAuthSeq(String scopes) {
        String appAuthSeq = ssoContext.getSsoLocalConfig().getProperty("SSO_AUTH_LOGIN_SEQUENCE");

        String authSeq = null;
        for (String scope : SSOUtils.scopeAsList(scopes)) {
            if (scope.startsWith("ovirt-ext=auth:sequence-priority=")) {
                String[] tokens = scope.trim().split("=", 3);
                authSeq = tokens[2];
            }
        }

        List<InteractiveAuth> authSeqList = getAuthListForSeq(authSeq);

        if (StringUtils.isNotEmpty(authSeq) && authSeq.startsWith("~")) {
            // get unique auth seq
            for (char c : appAuthSeq.toCharArray()) {
                if (!authSeqList.contains(InteractiveAuth.valueOf("" + c))) {
                    authSeqList.add(InteractiveAuth.valueOf("" + c));
                }
            }
            // intersect auth seq with sso auth seq settings
            authSeqList.retainAll(getAuthListForSeq(appAuthSeq));
        }
        Collections.reverse(authSeqList);
        Stack<InteractiveAuth> authSeqStack = new Stack<>();
        authSeqStack.addAll(authSeqList);
        return authSeqStack;
    }

    private static List<InteractiveAuth> getAuthListForSeq(String authSeq) {
        List<InteractiveAuth> authSeqList = new ArrayList<>();
        if (StringUtils.isNotEmpty(authSeq)) {
            for (char c : authSeq.toCharArray()) {
                if (c == '~') {
                    continue;
                }
                authSeqList.add(InteractiveAuth.valueOf("" + c));
            }
        }
        return authSeqList;
    }
}
