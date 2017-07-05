package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.sso.utils.AuthenticationException;
import org.ovirt.engine.core.sso.utils.OAuthException;
import org.ovirt.engine.core.sso.utils.SsoConstants;
import org.ovirt.engine.core.sso.utils.SsoSession;
import org.ovirt.engine.core.sso.utils.SsoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenIdUserInfoServlet extends HttpServlet {
    private static final long serialVersionUID = 7168485079055058668L;
    private static Logger log = LoggerFactory.getLogger(OpenIdUserInfoServlet.class);

    private static final String BEARER = "Bearer";

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            log.debug("Entered OpenIdUserInfoServlet Query String: {}, Parameters : {}",
                    request.getQueryString(),
                    SsoUtils.getRequestParameters(request));
            String token = request.getParameter(SsoConstants.HTTP_REQ_ATTR_ACCESS_TOKEN);
            if (token == null) {
                token = getTokenFromHeader(request);
            }
            if (token == null) {
                throw new OAuthException(SsoConstants.ERROR,
                        SsoConstants.ERR_CODE_INVALID_REQUEST);
            }
            SsoSession ssoSession = SsoUtils.getSsoSessionFromRequest(request, token);
            if (!ssoSession.isActive()) {
                throw new OAuthException(SsoConstants.ERR_CODE_INVALID_TOKEN,
                        SsoConstants.ERR_SESSION_EXPIRED_MSG);
            }
            SsoUtils.sendJsonData(response, buildResponse(request, ssoSession), "application/jwt");
        } catch(OAuthException ex) {
            SsoUtils.sendJsonDataWithMessage(request, response, ex);
        } catch(AuthenticationException ex) {
            SsoUtils.sendJsonDataWithMessage(request, response, SsoConstants.ERR_CODE_ACCESS_DENIED, ex);
        } catch(Exception ex) {
            SsoUtils.sendJsonDataWithMessage(request, response, SsoConstants.ERR_CODE_SERVER_ERROR, ex);
        }

    }

    private String getTokenFromHeader(HttpServletRequest request) {
        String token = null;
        String headerValue = request.getHeader(SsoConstants.HEADER_AUTHORIZATION);
        if (headerValue != null && headerValue.startsWith(BEARER)) {
            token = headerValue.substring(BEARER.length()).trim();
        }
        return token;
    }

    private String buildResponse(HttpServletRequest request, SsoSession ssoSession) throws Exception {
        return OpenIdUtils.createJWT(request, ssoSession, ssoSession.getClientId()).toString();
    }
}
