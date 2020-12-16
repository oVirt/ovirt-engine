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
import org.apache.http.HttpStatus;
import org.ovirt.engine.core.sso.api.InteractiveAuth;
import org.ovirt.engine.core.sso.api.OAuthBadRequestException;
import org.ovirt.engine.core.sso.api.OAuthException;
import org.ovirt.engine.core.sso.api.SsoConstants;
import org.ovirt.engine.core.sso.api.SsoContext;
import org.ovirt.engine.core.sso.api.SsoSession;
import org.ovirt.engine.core.sso.service.SsoService;
import org.ovirt.engine.core.uutils.net.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthAuthorizeServlet extends HttpServlet {
    private static final long serialVersionUID = -4822437649213489822L;
    private static Logger log = LoggerFactory.getLogger(OAuthAuthorizeServlet.class);

    private SsoContext ssoContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ssoContext = SsoService.getSsoContext(config.getServletContext());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            handleRequest(request, response);
        } catch (OAuthBadRequestException ex) {
            response.sendError(HttpStatus.SC_BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            SsoService.redirectToErrorPage(request, response, ex);
        }
    }

    protected void handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("Entered AuthorizeServlet QueryString: {}, Parameters : {}",
                request.getQueryString(),
                SsoService.getRequestParameters(request));
        String responseType = SsoService.getRequestParameter(request, SsoConstants.JSON_RESPONSE_TYPE, true);

        if (!responseType.equals("code")) {
            throw new OAuthBadRequestException(SsoConstants.ERR_CODE_INVALID_REQUEST,
                    String.format(
                            ssoContext.getLocalizationUtils()
                                    .localize(
                                            SsoConstants.APP_ERROR_UNSUPPORTED_PARAMETER_IN_REQUEST,
                                            (Locale) request.getAttribute(SsoConstants.LOCALE)),
                            SsoConstants.JSON_RESPONSE_TYPE));
        }
        login(request, response, buildSsoSession(request));
    }

    protected SsoSession buildSsoSession(HttpServletRequest request)
            throws Exception {
        String clientId = SsoService.getRequestParameter(request, SsoConstants.HTTP_PARAM_CLIENT_ID, true);
        String redirectUri = request.getParameter(SsoConstants.HTTP_PARAM_REDIRECT_URI);
        String scope = SsoService.getScopeRequestParameter(request, "");
        SsoService.validateRedirectUri(request, clientId, redirectUri, scope);
        String state = SsoService.getRequestParameter(request, SsoConstants.HTTP_PARAM_STATE, "");
        String appUrl = SsoService.getRequestParameter(request, SsoConstants.HTTP_PARAM_APP_URL, "");
        String sourceAddr = SsoService.getRequestParameter(request, SsoConstants.HTTP_PARAM_SOURCE_ADDR, "UNKNOWN");
        validateClientRequest(request, clientId, scope, redirectUri);

        // Create the session
        request.getSession(true);

        SsoSession ssoSession = SsoService.getSsoSession(request);
        ssoSession.setAppUrl(appUrl);
        ssoSession.setClientId(clientId);
        ssoSession.setSourceAddr(sourceAddr);
        ssoSession.setRedirectUri(redirectUri);
        ssoSession.setScope(scope);
        ssoSession.setState(state);
        ssoSession.getHttpSession().setMaxInactiveInterval(-1);

        return ssoSession;
    }

    protected void validateClientRequest(HttpServletRequest request,
            String clientId,
            String scope,
            String redirectUri) {
        SsoService.validateClientRequest(request, clientId, null, scope, redirectUri);
    }

    protected void login(
            HttpServletRequest request,
            HttpServletResponse response,
            SsoSession ssoSession) throws Exception {
        log.debug("Entered login queryString: {}", request.getQueryString());
        String redirectUrl;

        if (SsoService.isUserAuthenticated(request)) {
            log.debug("User is authenticated redirecting to interactive-redirect-to-module");
            redirectUrl = request.getContextPath() + SsoConstants.INTERACTIVE_REDIRECT_TO_MODULE_URI;
        } else if (SsoService.scopeAsList(SsoService.getScopeRequestParameter(request, ""))
                .contains("ovirt-ext=auth:identity")) {
            redirectUrl = new URLBuilder(SsoService.getRedirectUrl(request))
                    .addParameter(SsoConstants.ERROR, SsoConstants.ERR_OVIRT_CODE_NOT_AUTHENTICATED)
                    .addParameter(SsoConstants.ERROR_DESCRIPTION, SsoConstants.ERR_CODE_NOT_AUTHENTICATED_MSG)
                    .build();
        } else {
            ssoSession.setAuthStack(getAuthSeq(ssoSession));
            if (ssoSession.getAuthStack().isEmpty()) {
                throw new OAuthException(SsoConstants.ERR_CODE_ACCESS_DENIED,
                        ssoContext.getLocalizationUtils()
                                .localize(
                                        SsoConstants.APP_ERROR_NO_VALID_AUTHENTICATION_MECHANISM_FOUND,
                                        (Locale) request.getAttribute(SsoConstants.LOCALE)));
            }
            redirectUrl = request.getContextPath() + SsoConstants.INTERACTIVE_LOGIN_NEXT_AUTH_URI;
        }
        log.debug("Redirecting to url: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    protected Stack<InteractiveAuth> getAuthSeq(SsoSession ssoSession) {
        String scopes = ssoSession.getScope();
        String appAuthSeq = ssoContext.getSsoLocalConfig().getProperty("SSO_AUTH_LOGIN_SEQUENCE");

        String authSeq = null;
        if (StringUtils.isEmpty(scopes) || !scopes.contains("ovirt-ext=auth:sequence-priority=")) {
            authSeq = "~";
        } else {
            for (String scope : SsoService.scopeAsList(scopes)) {
                if (scope.startsWith("ovirt-ext=auth:sequence-priority=")) {
                    String[] tokens = scope.trim().split("=", 3);
                    authSeq = tokens[2];
                }
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
