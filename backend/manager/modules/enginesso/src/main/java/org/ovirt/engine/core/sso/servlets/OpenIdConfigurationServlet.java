package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.conn.util.InetAddressUtils;
import org.ovirt.engine.core.sso.api.SsoConstants;
import org.ovirt.engine.core.sso.service.OpenIdService;
import org.ovirt.engine.core.sso.service.SsoService;

public class OpenIdConfigurationServlet extends HttpServlet {

    @Inject
    private Instance<OpenIdService> openIdService;

    private static Map<String, Object> staticConfig = new HashMap<>();

    static {
        staticConfig.put("claim_types_supported", Collections.singletonList("normal"));
        staticConfig.put("claims_supported",
                Arrays.asList("sub",
                        "iss",
                        "auth_time",
                        "name",
                        "given_name",
                        "family_name",
                        "preferred_username",
                        "email"));
        staticConfig.put("grant_types_supported", Arrays.asList("authorization_code", "password"));
        staticConfig.put("id_token_signing_alg_values_supported", Arrays.asList("HS256", "RS256"));
        staticConfig.put("request_object_signing_alg_values_supported", Collections.singletonList("none"));
        staticConfig.put("request_parameter_supported", true);
        staticConfig.put("request_uri_parameter_supported", true);
        staticConfig.put("require_request_uri_registration", true);
        staticConfig.put("response_modes_supported", Arrays.asList("query", "fragment", "form_post"));
        staticConfig.put("response_types_supported", Collections.singletonList("code"));
        staticConfig.put("scopes_supported", Collections.singletonList("openid"));
        staticConfig.put("subject_types_supported", Arrays.asList("public", "pairwise"));
        staticConfig.put("token_endpoint_auth_methods_supported",
                Arrays.asList("client_secret_basic", "client_secret_post"));
        staticConfig.put("userinfo_signing_alg_values_supported", Collections.singletonList("RS256"));
        staticConfig.put("version", "1.0");
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            SsoService.sendJsonData(response, openIdService.get().getJson(buildResponse(request)));
        } catch (Exception ex) {
            SsoService.sendJsonDataWithMessage(request, response, SsoConstants.ERR_CODE_SERVER_ERROR, ex);
        }
    }

    private Map<String, Object> buildResponse(HttpServletRequest request) {
        String ssoUrl = String.format("%s://%s:%s%s",
                request.getScheme(),
                getRedirectUriServerName(request.getServerName()),
                request.getServerPort(),
                request.getContextPath());
        Map<String, Object> payload = new HashMap<>();
        payload.put("authorization_endpoint", String.format("%s/openid/authorize", ssoUrl));
        payload.put("end_session_endpoint", String.format("%s/oauth/revoke", ssoUrl));
        payload.put("issuer", String.format("%s/openid", ssoUrl));
        payload.put("jwks_uri", String.format("%s/openid/jwks", ssoUrl));
        payload.put("token_endpoint", String.format("%s/openid/token", ssoUrl));
        payload.put("userinfo_endpoint", String.format("%s/openid/userinfo", ssoUrl));
        payload.putAll(staticConfig);
        return payload;
    }

    public static String getRedirectUriServerName(String name) {
        return InetAddressUtils.isIPv6Address(name) ? String.format("[%s]", name) : name;
    }

}
