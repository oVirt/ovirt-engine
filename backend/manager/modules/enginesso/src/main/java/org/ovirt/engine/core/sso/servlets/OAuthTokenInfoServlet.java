package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.sso.search.DirectorySearch;
import org.ovirt.engine.core.sso.utils.OAuthException;
import org.ovirt.engine.core.sso.utils.SSOConstants;
import org.ovirt.engine.core.sso.utils.SSOContext;
import org.ovirt.engine.core.sso.utils.SSOSession;
import org.ovirt.engine.core.sso.utils.SSOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthTokenInfoServlet extends HttpServlet {
    private static final long serialVersionUID = 5190618483759215735L;

    private static Logger log = LoggerFactory.getLogger(OAuthTokenInfoServlet.class);
    private Map<String, DirectorySearch> directoryQueries = new HashMap<>();
    private Map<String, DirectorySearch> directoryPublicQueries = new HashMap<>();
    private SSOContext ssoContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ssoContext = SSOUtils.getSsoContext(config.getServletContext());
        for (DirectorySearch query : DirectorySearch.values()) {
            if (query.isPublicQuery()) {
                directoryPublicQueries.put(query.getName(), query);
            } else {
                directoryQueries.put(query.getName(), query);
            }
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("Entered OAuthTokenInfo QueryString: {}, Parameters : {}",
                request.getQueryString(),
                SSOUtils.getRequestParameters(request));
        boolean isValidateRequest = false;
        boolean isSearchAuthzRequest;
        boolean isPublicSearchAuthzRequest;
        try {
            String[] clientIdAndSecret = SSOUtils.getClientIdClientSecret(request);

            String scope = SSOUtils.getRequestParameter(request, SSOConstants.HTTP_PARAM_SCOPE, "");
            isValidateRequest = SSOUtils.scopeAsList(scope).contains(SSOConstants.VALIDATE_SCOPE);
            isSearchAuthzRequest = SSOUtils.scopeAsList(scope).contains(SSOConstants.AUTHZ_SEARCH_SCOPE);
            isPublicSearchAuthzRequest = SSOUtils.scopeAsList(scope).contains(SSOConstants.PUBLIC_AUTHZ_SEARCH_SCOPE);
            SSOUtils.validateClientAcceptHeader(request);
            SSOUtils.validateClientRequest(request, clientIdAndSecret[0], clientIdAndSecret[1], null, null);

            if (isSearchAuthzRequest || isPublicSearchAuthzRequest) {
                validateQueryType(request);
            }

            if (!isPublicSearchAuthzRequest) {
                String token = SSOUtils.getRequestParameter(request, SSOConstants.HTTP_PARAM_TOKEN);
                SSOUtils.validateRequestScope(request, token, scope);
                SSOUtils.getSsoSession(request, clientIdAndSecret[0], token, true)
                        .getAssociatedClientIds()
                        .add(clientIdAndSecret[0]);
            }

            log.debug("Sending json response");
            SSOUtils.sendJsonData(response, isValidateRequest ?
                    Collections.<String, Object>emptyMap() :
                    isSearchAuthzRequest || isPublicSearchAuthzRequest ?
                            buildSearchResponse(request, isPublicSearchAuthzRequest) :
                            buildResponse(request, clientIdAndSecret[0], scope));
        } catch(OAuthException ex) {
            SSOUtils.sendJsonDataWithMessage(response, ex, isValidateRequest);
        } catch(Exception ex) {
            SSOUtils.sendJsonDataWithMessage(response, SSOConstants.ERR_CODE_SERVER_ERROR, ex);
        }
    }

    private void validateQueryType(HttpServletRequest request) throws Exception {
        String queryType = SSOUtils.getRequestParameter(request, SSOConstants.HTTP_PARAM_SEARCH_QUERY_TYPE);
        if (!directoryQueries.containsKey(queryType) && !directoryPublicQueries.containsKey(queryType)) {
            throw new OAuthException(SSOConstants.ERR_CODE_INVALID_REQUEST,
                    String.format(
                            ssoContext.getLocalizationUtils().localize(
                                    SSOConstants.APP_ERROR_UNSUPPORTED_PARAMETER_IN_REQUEST,
                                    (Locale) request.getAttribute(SSOConstants.LOCALE)),
                            queryType,
                            SSOConstants.HTTP_PARAM_SEARCH_QUERY_TYPE));
        }
    }

    private Map<String, Object> buildSearchResponse(HttpServletRequest request,
                                                    boolean isPublicSearchAuthzRequest) throws Exception {
        log.debug("Entered SearchDirectoryServlet Query String: {}, Parameters : {}",
                request.getQueryString(),
                SSOUtils.getRequestParameters(request));
        Map<String, Object> data = new HashMap<>();
        data.put("result",
                isPublicSearchAuthzRequest ?
                        directoryPublicQueries.get(
                                SSOUtils.getRequestParameter(request,
                                        SSOConstants.HTTP_PARAM_SEARCH_QUERY_TYPE)).executeQuery(ssoContext, request) :
                        directoryQueries.get(
                                SSOUtils.getRequestParameter(request,
                                        SSOConstants.HTTP_PARAM_SEARCH_QUERY_TYPE)).executeQuery(ssoContext, request));
        return data;
    }


    private Map<String, Object> buildResponse(HttpServletRequest request, String clientId, String scope) throws Exception {
        String token = SSOUtils.getRequestParameter(request, SSOConstants.HTTP_PARAM_TOKEN);
        SSOSession ssoSession = SSOUtils.getSsoSession(request, clientId, token, true);
        String password = null;
        if (SSOUtils.scopeAsList(scope).contains(SSOConstants.PASSWORD_ACCESS_SCOPE)) {
            password = ssoSession.getPassword();
        }
        return buildResponse(ssoSession, password);
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
        ovirt.put("group_ids", ssoSession.getPrincipalRecord().<Collection>get(Authz.PrincipalRecord.GROUPS,
                Collections.<ExtMap>emptyList()));
        if (password != null) {
            ovirt.put("password", password);
        }
        ovirt.put("capability_credentials_change",
                ssoContext.getSsoProfilesSupportingPasswdChange().contains(ssoSession.getProfile()));
        payload.put("ovirt", ovirt);
        return payload;
    }
}
