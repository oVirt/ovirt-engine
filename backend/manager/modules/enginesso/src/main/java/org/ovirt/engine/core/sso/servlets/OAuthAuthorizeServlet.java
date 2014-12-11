package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.sso.utils.InteractiveAuth;
import org.ovirt.engine.core.sso.utils.OAuthException;
import org.ovirt.engine.core.sso.utils.SSOConstants;
import org.ovirt.engine.core.sso.utils.SSOSession;
import org.ovirt.engine.core.sso.utils.SSOUtils;
import org.ovirt.engine.core.uutils.net.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthAuthorizeServlet extends HttpServlet {
    private static final long serialVersionUID = -4822437649213489822L;
    private static Logger log = LoggerFactory.getLogger(OAuthAuthorizeServlet.class);

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
                        String.format("The request contains unsupported parameter value '%s' for parameter '%s'.",
                                responseType, SSOConstants.JSON_RESPONSE_TYPE));
            }

            login(request, response, clientId, scope, state, redirectUri);
        } catch (Exception ex) {
            SSOUtils.redirectToErrorPage(request, response, ex);
        }
    }

    private static void login(HttpServletRequest request, HttpServletResponse response, String clientId, String scope, String state, String redirectUri) throws Exception {
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
            log.debug("User is authenticated redirecting to LoginPhase4Servlet");
            redirectUrl = request.getContextPath() + SSOConstants.INTERACTIVE_REDIRECT_TO_MODULE_URI;
        } else if (SSOUtils.scopeAsList(scope).contains("ovirt-ext=auth:identity")) {
            redirectUrl = new URLBuilder(SSOUtils.getRedirectUrl(request))
                    .addParameter("error_code", SSOConstants.ERR_OVIRT_CODE_NOT_AUTHENTICATED)
                    .addParameter("error", SSOConstants.ERR_CODE_NOT_AUTHENTICATED_MSG).build();
        } else {
            ssoSession.setAuthStack(getAuthSeq(request, scope));
            if (ssoSession.getAuthStack().isEmpty()) {
                throw new OAuthException(SSOConstants.ERR_CODE_ACCESS_DENIED, "No valid authentication mechanism found.");
            }
            redirectUrl = request.getContextPath() + SSOConstants.INTERACTIVE_LOGIN_NEXT_AUTH_URI;
        }
        log.debug("Redirecting to url: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    private static Stack<InteractiveAuth> getAuthSeq(HttpServletRequest request, String scopes) {
        String appAuthSeq = SSOUtils.getSsoContext(request).getSsoLocalConfig().getProperty("SSO_AUTH_LOGIN_SEQUENCE");

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
