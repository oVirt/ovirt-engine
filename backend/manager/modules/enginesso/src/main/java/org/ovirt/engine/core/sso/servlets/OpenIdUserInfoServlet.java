package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.sso.api.SsoConstants;
import org.ovirt.engine.core.sso.api.SsoContext;
import org.ovirt.engine.core.sso.api.SsoSession;
import org.ovirt.engine.core.sso.service.OpenIdService;
import org.ovirt.engine.core.sso.service.SsoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenIdUserInfoServlet extends HttpServlet {
    private static final long serialVersionUID = 7168485079055058668L;
    private static final String BEARER = "Bearer";
    private static Logger log = LoggerFactory.getLogger(OpenIdUserInfoServlet.class);

    @Inject
    @SuppressWarnings("unused")
    private Instance<OpenIdService> openIdService;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            log.debug("Entered OpenIdUserInfoServlet Query String: {}, Parameters : {}",
                    request.getQueryString(),
                    SsoService.getRequestParameters(request));
            String token = request.getParameter(SsoConstants.HTTP_REQ_ATTR_ACCESS_TOKEN);
            if (token == null) {
                token = getTokenFromHeader(request);
            }
            if (token == null) {
                SsoService.sendJsonDataWithMessage(request,
                        response,
                        SsoConstants.ERROR,
                        SsoConstants.ERR_CODE_INVALID_REQUEST,
                        false);
                return;
            }
            SsoSession ssoSession = SsoService.getSsoSessionFromRequest(request, token);
            if (!ssoSession.isActive()) {
                SsoService.sendJsonDataWithMessage(request,
                        response,
                        SsoConstants.ERR_CODE_INVALID_TOKEN,
                        SsoConstants.ERR_SESSION_EXPIRED_MSG,
                        false);
                return;
            }

            if (isEncryptedUserInfo(request, ssoSession)) {
                SsoService.sendJsonData(response, buildEncodedJWTResponse(request, ssoSession), "application/jwt");
            } else {
                SsoService.sendJsonData(response, buildPlainJsonResponse(request, ssoSession));
            }
        } catch (Exception ex) {
            SsoService.sendJsonDataWithMessage(request, response, SsoConstants.ERR_CODE_SERVER_ERROR, ex);
        }

    }

    private boolean isEncryptedUserInfo(HttpServletRequest request, SsoSession ssoSession) {
        if (StringUtils.isBlank(ssoSession.getClientId())) {
            // it should be encrypted by default
            return true;
        }
        SsoContext ssoContext = SsoService.getSsoContext(request);
        return ssoContext.getClienInfo(ssoSession.getClientId()).isEncryptedUserInfo();
    }

    private String getTokenFromHeader(HttpServletRequest request) {
        String token = null;
        String headerValue = request.getHeader(SsoConstants.HEADER_AUTHORIZATION);
        if (headerValue != null && headerValue.startsWith(BEARER)) {
            token = headerValue.substring(BEARER.length()).trim();
        }
        return token;
    }

    private String buildEncodedJWTResponse(HttpServletRequest request, SsoSession ssoSession) {
        return openIdService.get().createJWT(request, ssoSession, ssoSession.getClientId());
    }

    private String buildPlainJsonResponse(HttpServletRequest request, SsoSession ssoSession) {
        return openIdService.get().createUnencodedJWT(request, ssoSession, ssoSession.getClientId());
    }
}
