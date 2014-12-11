package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.sso.utils.OAuthException;
import org.ovirt.engine.core.sso.utils.SSOConstants;
import org.ovirt.engine.core.sso.utils.SSOSession;
import org.ovirt.engine.core.sso.utils.SSOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthTokenInfoServlet extends HttpServlet {
    private static final long serialVersionUID = 5190618483759215735L;
    private static Logger log = LoggerFactory.getLogger(OAuthTokenInfoServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("Entered OAuthTokenInfo QueryString: {}, Parameters : {}",
                request.getQueryString(),
                SSOUtils.getRequestParameters(request));
        boolean isValidateRequest = false;
        try {
            String[] clientIdAndSecret = SSOUtils.getClientIdClientSecret(request);
            String token = SSOUtils.getRequestParameter(request, SSOConstants.HTTP_PARAM_TOKEN);
            String scope = SSOUtils.getRequestParameter(request, SSOConstants.HTTP_PARAM_SCOPE, "");
            isValidateRequest = SSOUtils.scopeAsList(scope).contains("ovirt-ext=token-info:validate");
            SSOUtils.validateClientAcceptHeader(request);
            SSOUtils.validateClientRequest(request, clientIdAndSecret[0], clientIdAndSecret[1], null, null);
            SSOUtils.validateRequestScope(request, token, scope);
            log.debug("Sending json response");
            SSOSession ssoSession = SSOUtils.getSsoSession(request, clientIdAndSecret[0], token, true);
            ssoSession.getAssociatedClientIds().add(clientIdAndSecret[0]);
            String password = null;
            if (SSOUtils.scopeAsList(scope).contains("ovirt-ext=token:password-access")) {
                password = ssoSession.getPassword();
            }
            SSOUtils.sendJsonData(response, isValidateRequest ?
                    Collections.<String, Object>emptyMap() :
                    buildResponse(ssoSession, password));
        } catch(OAuthException ex) {
            SSOUtils.sendJsonDataWithMessage(response, ex, isValidateRequest);
        } catch(Exception ex) {
            SSOUtils.sendJsonDataWithMessage(response, SSOConstants.ERR_CODE_SERVER_ERROR, ex);
        }
    }

    private Map<String, Object> buildResponse(SSOSession ssoSession, String password) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(SSOConstants.JSON_ACTIVE, ssoSession.isActive());
        payload.put(SSOConstants.JSON_TOKEN_TYPE, "bearer");
        payload.put(SSOConstants.JSON_CLIENT_ID, ssoSession.getClientId());
        payload.put(SSOConstants.JSON_USER_ID, String.format("%s@%s", ssoSession.getUserId(),
                ssoSession.getProfile()));
        payload.put(SSOConstants.JSON_SCOPE, StringUtils.isEmpty(ssoSession.getScope()) ? "" : ssoSession.getScope());
        payload.put(SSOConstants.JSON_EXPIRES_IN, ssoSession.getValidTo());

        Map<String, Object> ovirt = new HashMap<>();
        ovirt.put("version", SSOConstants.OVIRT_SSO_VERSION);
        ovirt.put("principal_id", ssoSession.getPrincipalRecord().get(Authz.PrincipalRecord.ID));
        ovirt.put("email", ssoSession.getPrincipalRecord().get(Authz.PrincipalRecord.EMAIL));
        ovirt.put("group_ids", ssoSession.getPrincipalRecord().<List>get(Authz.PrincipalRecord.GROUPS, Collections.<ExtMap>emptyList()));
        if (password != null) {
            ovirt.put("password", password);
        }
        payload.put("ovirt", ovirt);
        return payload;
    }
}
