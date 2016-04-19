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
import org.ovirt.engine.core.sso.utils.SsoConstants;
import org.ovirt.engine.core.sso.utils.SsoContext;
import org.ovirt.engine.core.sso.utils.SsoSession;
import org.ovirt.engine.core.sso.utils.SsoUtils;
import org.ovirt.engine.core.uutils.net.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthAuthorizeServlet extends HttpServlet {
    private static final long serialVersionUID = -4822437649213489822L;
    private static Logger log = LoggerFactory.getLogger(OAuthAuthorizeServlet.class);

    private SsoContext ssoContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ssoContext = SsoUtils.getSsoContext(config.getServletContext());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            log.debug("Entered AuthorizeServlet QueryString: {}, Parameters : {}",
                    request.getQueryString(),
                    SsoUtils.getRequestParameters(request));
            String clientId = SsoUtils.getRequestParameter(request, SsoConstants.HTTP_PARAM_CLIENT_ID);
            String responseType = SsoUtils.getRequestParameter(request, SsoConstants.JSON_RESPONSE_TYPE);
            String scope = SsoUtils.getScopeRequestParameter(request, "");
            String state = SsoUtils.getRequestParameter(request, SsoConstants.HTTP_PARAM_STATE, "");
            String redirectUri = SsoUtils.getParameter(request, SsoConstants.HTTP_PARAM_REDIRECT_URI);
            SsoUtils.validateClientRequest(request, clientId, null, scope, redirectUri);

            if (!responseType.equals("code")) {
                throw new OAuthException(SsoConstants.ERR_CODE_INVALID_REQUEST,
                        String.format(
                                ssoContext.getLocalizationUtils().localize(
                                        SsoConstants.APP_ERROR_UNSUPPORTED_PARAMETER_IN_REQUEST,
                                        (Locale) request.getAttribute(SsoConstants.LOCALE)),
                                responseType,
                                SsoConstants.JSON_RESPONSE_TYPE));
            }

            login(request, response, clientId, scope, state, redirectUri);
        } catch (Exception ex) {
            SsoUtils.redirectToErrorPage(request, response, ex);
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

        SsoSession ssoSession = SsoUtils.getSsoSession(request);
        ssoSession.setClientId(clientId);
        ssoSession.setRedirectUri(redirectUri);
        ssoSession.setScope(scope);
        ssoSession.setState(state);
        ssoSession.getHttpSession().setMaxInactiveInterval(-1);

        if (SsoUtils.isUserAuthenticated(request)) {
            log.debug("User is authenticated redirecting to interactive-redirect-to-module");
            redirectUrl = request.getContextPath() + SsoConstants.INTERACTIVE_REDIRECT_TO_MODULE_URI;
        } else if (SsoUtils.scopeAsList(scope).contains("ovirt-ext=auth:identity")) {
            redirectUrl = new URLBuilder(SsoUtils.getRedirectUrl(request))
                    .addParameter("error_code", SsoConstants.ERR_OVIRT_CODE_NOT_AUTHENTICATED)
                    .addParameter("error", SsoConstants.ERR_CODE_NOT_AUTHENTICATED_MSG).build();
        } else {
            ssoSession.setAuthStack(getAuthSeq(scope));
            if (ssoSession.getAuthStack().isEmpty()) {
                throw new OAuthException(SsoConstants.ERR_CODE_ACCESS_DENIED,
                        ssoContext.getLocalizationUtils().localize(
                                SsoConstants.APP_ERROR_NO_VALID_AUTHENTICATION_MECHANISM_FOUND,
                                (Locale) request.getAttribute(SsoConstants.LOCALE)));
            }
            redirectUrl = request.getContextPath() + SsoConstants.INTERACTIVE_LOGIN_NEXT_AUTH_URI;
        }
        log.debug("Redirecting to url: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    private Stack<InteractiveAuth> getAuthSeq(String scopes) {
        String appAuthSeq = ssoContext.getSsoLocalConfig().getProperty("SSO_AUTH_LOGIN_SEQUENCE");

        String authSeq = null;
        for (String scope : SsoUtils.scopeAsList(scopes)) {
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
