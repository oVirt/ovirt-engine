package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.sso.api.OAuthException;
import org.ovirt.engine.core.sso.api.SsoConstants;
import org.ovirt.engine.core.sso.api.SsoContext;
import org.ovirt.engine.core.sso.api.SsoSession;
import org.ovirt.engine.core.sso.search.DirectorySearch;
import org.ovirt.engine.core.sso.service.SsoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthTokenInfoServlet extends HttpServlet {
    private static final long serialVersionUID = 5190618483759215735L;

    private static Logger log = LoggerFactory.getLogger(OAuthTokenInfoServlet.class);
    private Map<String, DirectorySearch> directoryQueries = new HashMap<>();
    private Map<String, DirectorySearch> directoryPublicQueries = new HashMap<>();
    private SsoContext ssoContext;

    @Override
    public void init(ServletConfig config) {
        ssoContext = SsoService.getSsoContext(config.getServletContext());
        for (DirectorySearch query : DirectorySearch.values()) {
            if (query.isPublicQuery()) {
                directoryPublicQueries.put(query.getName(), query);
            } else {
                directoryQueries.put(query.getName(), query);
            }
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.debug("Entered OAuthTokenInfo QueryString: {}, Parameters : {}",
                request.getQueryString(),
                SsoService.getRequestParameters(request));
        boolean isValidateRequest = false;
        boolean isSearchAuthzRequest;
        boolean isPublicSearchAuthzRequest;
        try {
            String scope = SsoService.getRequestParameter(request, SsoConstants.HTTP_PARAM_SCOPE, "");
            isValidateRequest = SsoService.scopeAsList(scope).contains(SsoConstants.VALIDATE_SCOPE);
            isSearchAuthzRequest = SsoService.scopeAsList(scope).contains(SsoConstants.AUTHZ_SEARCH_SCOPE);
            isPublicSearchAuthzRequest = SsoService.scopeAsList(scope).contains(SsoConstants.PUBLIC_AUTHZ_SEARCH_SCOPE);
            SsoService.validateClientAcceptHeader(request);

            if (isValidateRequest) {
                String token = SsoService.getRequestParameter(request, SsoConstants.HTTP_PARAM_TOKEN);
                SsoService.getSsoSession(request, null, token, true);
                log.debug("Sending json response");
                SsoService.sendJsonData(response, Collections.emptyMap());
            } else {
                String[] clientIdAndSecret = SsoService.getClientIdClientSecret(request);
                SsoService.validateClientRequest(request, clientIdAndSecret[0], clientIdAndSecret[1], null, null);

                if (isSearchAuthzRequest || isPublicSearchAuthzRequest) {
                    validateQueryType(request);
                }

                if (!isPublicSearchAuthzRequest) {
                    String token = SsoService.getRequestParameter(request, SsoConstants.HTTP_PARAM_TOKEN);
                    SsoService.validateRequestScope(request, token, scope);
                    SsoService.getSsoSession(request, clientIdAndSecret[0], token, true)
                            .getAssociatedClientIds()
                            .add(clientIdAndSecret[0]);
                }

                log.debug("Sending json response");
                SsoService.sendJsonData(response,
                        isSearchAuthzRequest || isPublicSearchAuthzRequest
                                ? buildSearchResponse(request, isPublicSearchAuthzRequest)
                                : buildResponse(request, clientIdAndSecret[0], scope));
            }
        } catch (OAuthException ex) {
            SsoService.sendJsonDataWithMessage(request, response, ex, isValidateRequest);
        } catch (Exception ex) {
            SsoService.sendJsonDataWithMessage(request, response, SsoConstants.ERR_CODE_SERVER_ERROR, ex);
        }
    }

    private void validateQueryType(HttpServletRequest request) {
        String queryType = SsoService.getRequestParameter(request, SsoConstants.HTTP_PARAM_SEARCH_QUERY_TYPE);
        if (!directoryQueries.containsKey(queryType) && !directoryPublicQueries.containsKey(queryType)) {
            throw new OAuthException(SsoConstants.ERR_CODE_INVALID_REQUEST,
                    String.format(
                            ssoContext.getLocalizationUtils()
                                    .localize(
                                            SsoConstants.APP_ERROR_UNSUPPORTED_PARAMETER_IN_REQUEST,
                                            (Locale) request.getAttribute(SsoConstants.LOCALE)),
                            SsoConstants.HTTP_PARAM_SEARCH_QUERY_TYPE));
        }
    }

    private Map<String, Object> buildSearchResponse(HttpServletRequest request,
            boolean isPublicSearchAuthzRequest) throws Exception {
        log.debug("Entered SearchDirectoryServlet Query String: {}, Parameters : {}",
                request.getQueryString(),
                SsoService.getRequestParameters(request));
        Map<String, Object> data = new HashMap<>();
        data.put("result",
                isPublicSearchAuthzRequest ? directoryPublicQueries.get(
                        SsoService.getRequestParameter(request,
                                SsoConstants.HTTP_PARAM_SEARCH_QUERY_TYPE))
                        .executeQuery(ssoContext, request)
                        : directoryQueries.get(
                                SsoService.getRequestParameter(request,
                                        SsoConstants.HTTP_PARAM_SEARCH_QUERY_TYPE))
                                .executeQuery(ssoContext, request));
        return data;
    }

    private Map<String, Object> buildResponse(HttpServletRequest request, String clientId, String scope) {
        String token = SsoService.getRequestParameter(request, SsoConstants.HTTP_PARAM_TOKEN);
        SsoSession ssoSession = SsoService.getSsoSession(request, clientId, token, true);
        String password = null;
        if (SsoService.scopeAsList(scope).contains(SsoConstants.PASSWORD_ACCESS_SCOPE)) {
            password = ssoSession.getPassword();
        }
        return buildResponse(ssoSession, password);
    }

    private Map<String, Object> buildResponse(SsoSession ssoSession, String password) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(SsoConstants.JSON_ACTIVE, ssoSession.isActive());
        payload.put(SsoConstants.JSON_TOKEN_TYPE, "bearer");
        payload.put(SsoConstants.JSON_CLIENT_ID, ssoSession.getClientId());
        payload.put(SsoConstants.JSON_USER_ID, ssoSession.getUserIdWithProfile());
        payload.put(SsoConstants.JSON_USER_AUTHZ, ssoContext.getUserAuthzName(ssoSession));
        payload.put(SsoConstants.JSON_SCOPE, StringUtils.isEmpty(ssoSession.getScope()) ? "" : ssoSession.getScope());
        payload.put(SsoConstants.JSON_EXPIRES_IN, ssoSession.getValidTo().toString());

        Map<String, Object> ovirt = new HashMap<>();
        ovirt.put("version", SsoConstants.OVIRT_SSO_VERSION);
        ovirt.put("principal_id", ssoSession.getPrincipalRecord().<String> get(Authz.PrincipalRecord.ID));
        ovirt.put("email", ssoSession.getPrincipalRecord().<String> get(Authz.PrincipalRecord.EMAIL));
        ovirt.put("namespace", ssoSession.getPrincipalRecord().<String> get(Authz.PrincipalRecord.NAMESPACE));
        ovirt.put("first_name", ssoSession.getPrincipalRecord().<String> get(Authz.PrincipalRecord.FIRST_NAME));
        ovirt.put("last_name", ssoSession.getPrincipalRecord().<String> get(Authz.PrincipalRecord.LAST_NAME));
        ovirt.put("group_ids",
                ssoSession.getPrincipalRecord()
                        .<Collection> get(Authz.PrincipalRecord.GROUPS,
                                Collections.<ExtMap> emptyList()));
        if (password != null) {
            ovirt.put("password", password);
        }
        ovirt.put("capability_credentials_change",
                ssoContext.getSsoProfilesSupportingPasswdChange().contains(ssoSession.getProfile()));
        payload.put("ovirt", ovirt);
        return payload;
    }
}
