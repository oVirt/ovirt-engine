package org.ovirt.engine.core.sso.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.net.ssl.TrustManagerFactory;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.uutils.crypto.EnvelopeEncryptDecrypt;
import org.ovirt.engine.core.uutils.crypto.EnvelopePBE;
import org.ovirt.engine.core.uutils.net.HttpURLConnectionBuilder;
import org.ovirt.engine.core.uutils.net.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoUtils {
    private static Logger log = LoggerFactory.getLogger(SsoUtils.class);

    public static boolean isUserAuthenticated(HttpServletRequest request) {
        return getSsoSession(request).getStatus() == SsoSession.Status.authenticated;
    }

    public static void redirectToModule(HttpServletRequest request,
                                        HttpServletResponse response)
            throws IOException {
        log.debug("Entered redirectToModule");
        try {
            SsoSession ssoSession = getSsoSession(request);
            URLBuilder redirectUrl = new URLBuilder(getRedirectUrl(request).toString())
                    .addParameter("code", ssoSession.getAuthorizationCode());
            String state = ssoSession.getState();
            if (StringUtils.isNotEmpty(state)) {
                redirectUrl.addParameter("state", state);
            }
            response.sendRedirect(redirectUrl.build());
            log.debug("Redirecting back to module: {}", redirectUrl);
        } catch (Exception ex) {
            log.error("Error redirecting back to module: {}", ex.getMessage());
            log.debug("Exception", ex);
            throw new RuntimeException(ex);
        } finally {
            SsoUtils.getSsoSession(request).cleanup();
        }
    }

    public static String getRedirectUrl(HttpServletRequest request) throws Exception {
        String uri = getSsoSession(request, true).getRedirectUri();
        return StringUtils.isEmpty(uri) ?
                new URLBuilder(getSsoContext(request).getEngineUrl(), "/oauth2-callback").build()  : uri;
    }

    public static void redirectToErrorPage(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        log.error(ex.getMessage());
        log.debug("Exception", ex);
        redirectToErrorPageImpl(request,
                response,
                new OAuthException(SsoConstants.ERR_CODE_SERVER_ERROR, ex.getMessage(), ex));
    }

    public static void redirectToErrorPage(HttpServletRequest request,
                                           HttpServletResponse response,
                                           OAuthException ex) {
        log.error("OAuthException {}: {}", ex.getCode(), ex.getMessage());
        log.debug("Exception", ex);
        redirectToErrorPageImpl(request, response, ex);
    }

    private static void redirectToErrorPageImpl(
            HttpServletRequest request,
            HttpServletResponse response,
            OAuthException ex) {
        log.debug("Entered redirectToErrorPage");
        SsoSession ssoSession = null;
        try {
            ssoSession = SsoUtils.getSsoSession(request, true);
            if (ssoSession.getStatus() != SsoSession.Status.authenticated) {
                ssoSession.setStatus(SsoSession.Status.unauthenticated);
            }
            String redirectUrl = new URLBuilder(getRedirectUrl(request))
                    .addParameter("error_code", ex.getCode())
                    .addParameter("error", ex.getMessage()).build();
            response.sendRedirect(redirectUrl);
            log.debug("Redirecting back to module: {}", redirectUrl);
        } catch (Exception e) {
            log.error("Error redirecting to error page: {}", e.getMessage());
            log.debug("Exception", e);
            throw new RuntimeException(ex);
        } finally {
            if (ssoSession != null) {
                ssoSession.cleanup();
            }
        }
    }

    public static String generateAuthorizationToken() {
        String ssoTokenId;
        try {
            byte[] s = new byte[64];
            SecureRandom.getInstance("SHA1PRNG").nextBytes(s);
            ssoTokenId = new Base64(0, new byte[0], true).encodeToString(s);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return ssoTokenId;
    }

    public static String getJson(Object obj) throws IOException {
        ObjectMapper mapper = new ObjectMapper().configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        mapper.getSerializationConfig().addMixInAnnotations(ExtMap.class, JsonExtMapMixIn.class);
        return mapper.writeValueAsString(obj);
    }

    public static String[] getClientIdClientSecret(HttpServletRequest request) throws Exception {
        String[] retVal = new String[2];
        retVal[0] = getParameter(request, SsoConstants.HTTP_PARAM_CLIENT_ID);
        retVal[1] = getParameter(request, SsoConstants.HTTP_PARAM_CLIENT_SECRET);
        if (StringUtils.isEmpty(retVal[0]) && StringUtils.isEmpty(retVal[1])) {
            retVal = getClientIdClientSecretFromHeader(request);
        }
        if (StringUtils.isEmpty(retVal[0])) {
            throw new OAuthException(SsoConstants.ERR_CODE_INVALID_REQUEST,
                    String.format(SsoConstants.ERR_CODE_INVALID_REQUEST_MSG, SsoConstants.HTTP_PARAM_CLIENT_ID));
        }
        if (StringUtils.isEmpty(retVal[1])) {
            throw new OAuthException(SsoConstants.ERR_CODE_INVALID_REQUEST,
                    String.format(SsoConstants.ERR_CODE_INVALID_REQUEST_MSG, SsoConstants.HTTP_PARAM_CLIENT_SECRET));
        }
        return retVal;
    }

    public static String getClientId(HttpServletRequest request) {
        String clientId = null;
        String[] retVal = getClientIdClientSecretFromHeader(request);
        if (retVal != null &&
                StringUtils.isNotEmpty(retVal[0]) &&
                getSsoContext(request).getClienInfo(retVal[0]) != null) {
            clientId = retVal[0];
        }
        return clientId;
    }

    public static String[] getClientIdClientSecretFromHeader(HttpServletRequest request) {
        String[] retVal = new String[2];
        String header = request.getHeader(SsoConstants.HEADER_AUTHORIZATION);
        if (StringUtils.isNotEmpty(header) && header.startsWith("Basic")) {
            String[] creds = new String(
                    Base64.decodeBase64(header.substring("Basic".length())),
                    StandardCharsets.UTF_8
            ).split(":", 2);
            if (creds.length == 2) {
                retVal = creds;
            }
        }
        return retVal;
    }

    public static String getParameter(HttpServletRequest request, String paramName)
            throws UnsupportedEncodingException {
        String value = request.getParameter(paramName);
        return value == null ?
                null : decode(new String(value.getBytes("iso-8859-1")));
    }

    public static String getFormParameter(HttpServletRequest request, String paramName)
            throws UnsupportedEncodingException {
        String value = request.getParameter(paramName);
        return value == null ? null : new String(value.getBytes("iso-8859-1"));
    }

    public static String getRequestParameter(HttpServletRequest request, String paramName) throws Exception {
        String value = getParameter(request, paramName);
        if (value == null) {
            throw new OAuthException(SsoConstants.ERR_CODE_INVALID_REQUEST,
                    String.format(SsoConstants.ERR_CODE_INVALID_REQUEST_MSG, paramName));
        }
        return value;
    }

    public static String decode(String value) throws UnsupportedEncodingException {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
    }

    public static String getRequestParameters(HttpServletRequest request) {
        StringBuilder value = new StringBuilder("");
        try {
            Enumeration<String> paramNames  = request.getParameterNames();
            String paramName;
            while (paramNames.hasMoreElements()) {
                paramName = paramNames.nextElement();
                value.append(String.format("%s = %s, ", paramName, getRequestParameter(request, paramName)));
            }
        } catch (Exception ex) {
            log.debug("Unable to get parameters from request");
        }
        return value.toString();
    }

    public static String getRequestParameter(HttpServletRequest request, String paramName, String defaultValue) {
        String value;
        try {
            value = getRequestParameter(request, paramName);
        } catch (Exception ex) {
            log.debug("Parameter {} not found request, using default value", paramName);
            value = defaultValue;
        }
        return value;
    }

    public static String getScopeRequestParameter(HttpServletRequest request, String defaultValue) {
        return resolveScopeWithDependencies(getSsoContext(request),
                getRequestParameter(request, SsoConstants.HTTP_PARAM_SCOPE, defaultValue));
    }

    public static String resolveScopeWithDependencies(SsoContext context, String scopes) {
        Set<String> scopesSet = new TreeSet<>();
        for (String scope : scopeAsList(scopes)) {
            scopesSet.add(scope);
            scopesSet.addAll(context.getScopeDependencies(scope));
        }
        return StringUtils.join(scopesSet, " ");
    }

    public static SsoContext getSsoContext(HttpServletRequest request) {
        return (SsoContext) request.getServletContext().getAttribute(SsoConstants.OVIRT_SSO_CONTEXT);
    }

    public static SsoContext getSsoContext(ServletContext ctx) {
        return (SsoContext) ctx.getAttribute(SsoConstants.OVIRT_SSO_CONTEXT);
    }

    public static SsoSession getSsoSessionFromRequest(HttpServletRequest request, String token) {
        return getSsoSession(request, null, token, false);
    }

    public static SsoSession getSsoSession(HttpServletRequest request, String token, boolean mustExist) {
        return getSsoSession(request, null, token, mustExist);
    }

    public static SsoSession getSsoSession(
            HttpServletRequest request,
            String clientId,
            String token,
            boolean mustExist) {
        TokenCleanupUtility.cleanupExpiredTokens(request.getServletContext());
        SsoSession ssoSession = null;
        if (StringUtils.isNotEmpty(token)) {
            ssoSession = getSsoContext(request).getSsoSession(token);
            if (ssoSession != null) {
                ssoSession.touch();
            }
        }
        if (mustExist && ssoSession == null) {
            throw new OAuthException(SsoConstants.ERR_CODE_INVALID_GRANT,
                    "The provided authorization grant for the auth code has expired");
        }
        if (StringUtils.isNotEmpty(clientId) &&
                StringUtils.isNotEmpty(ssoSession.getClientId()) &&
                !ssoSession.getClientId().equals(clientId)) {
            throw new OAuthException(SsoConstants.ERR_CODE_UNAUTHORIZED_CLIENT,
                    SsoConstants.ERR_CODE_UNAUTHORIZED_CLIENT);
        }
        return ssoSession;
    }

    public static SsoSession getSsoSession(HttpServletRequest request) {
        SsoSession ssoSession = request.getSession(false) == null ?
                null : (SsoSession) request.getSession().getAttribute(SsoConstants.OVIRT_SSO_SESSION);
        if (ssoSession == null) {
            throw new OAuthException(SsoConstants.ERR_CODE_INVALID_GRANT, "Session expired please try again.");
        }
        return ssoSession;
    }

    public static SsoSession getSsoSession(HttpServletRequest request, boolean mustExist)
            throws UnsupportedEncodingException {
        SsoSession ssoSession = request.getSession(false) == null ?
                null : (SsoSession) request.getSession().getAttribute(SsoConstants.OVIRT_SSO_SESSION);
        if ((ssoSession == null || StringUtils.isEmpty(ssoSession.getClientId())) && mustExist) {
            ssoSession = ssoSession == null ? new SsoSession() : ssoSession;
            ssoSession.setClientId(getClientId(request));
            ssoSession.setScope(getScopeRequestParameter(request, ""));
            ssoSession.setState(getRequestParameter(request, SsoConstants.HTTP_PARAM_STATE, ""));
            ssoSession.setRedirectUri(getParameter(request, SsoConstants.HTTP_PARAM_REDIRECT_URI));
        }
        return ssoSession;
    }

    public static Credentials getUserCredentialsFromHeader(HttpServletRequest request) {
        String header = request.getHeader(SsoConstants.HEADER_AUTHORIZATION);
        Credentials credentials = null;
        if (StringUtils.isNotEmpty(header)) {
            String[] creds = new String(
                    Base64.decodeBase64(header.substring("Basic".length())),
                    StandardCharsets.UTF_8
            ).split(":", 2);
            if (creds.length == 2) {
                credentials = translateUser(creds[0], creds[1], getSsoContext(request));
            }
        }
        return credentials;
    }

    public static Credentials translateUser(String user, String password, SsoContext ssoContext) {
        Credentials credentials = new Credentials();
        String username = user;
        int separator = user.lastIndexOf("@");
        if (separator != -1) {
            username = user.substring(0, separator);
            String profile = user.substring(separator + 1);
            if (StringUtils.isNotEmpty(profile)) {
                credentials.setProfile(profile);
                credentials.setProfileValid(ssoContext.getSsoProfiles().contains(profile));
            }
        }
        credentials.setUsername(username);
        credentials.setPassword(password);
        return credentials;
    }

    public static String getUserId(ExtMap principalRecord) {
        String principal = principalRecord.get(Authz.PrincipalRecord.PRINCIPAL);
        return principal != null ? principal : principalRecord.<String>get(Authz.PrincipalRecord.NAME);
    }

    public static void persistUserPassword(
            HttpServletRequest request,
            SsoSession ssoSession,
            String password) {
        try {
            if (ssoSession.getScopeAsList().contains("ovirt-ext=token:password-access") &&
                    password != null &&
                    StringUtils.isNotEmpty(ssoSession.getClientId())) {
                ssoSession.setPassword(encrypt(request.getServletContext(), ssoSession.getClientId(), password));
            }
        } catch (Exception ex) {
            log.error("Unable to encrypt password: {}", ex.getMessage());
            log.debug("Exception", ex);
        }
    }

    public static SsoSession persistAuthInfoInContextWithToken(
            HttpServletRequest request,
            String password,
            String profileName,
            ExtMap authRecord,
            ExtMap principalRecord) throws Exception {
        String validTo = authRecord.get(Authn.AuthRecord.VALID_TO);
        String authCode = generateAuthorizationToken();
        String accessToken = generateAuthorizationToken();

        SsoSession ssoSession = getSsoSession(request, true);
        ssoSession.setAccessToken(accessToken);
        ssoSession.setAuthorizationCode(authCode);

        request.setAttribute(SsoConstants.HTTP_REQ_ATTR_ACCESS_TOKEN, accessToken);

        ssoSession.setActive(true);
        ssoSession.setAuthRecord(authRecord);
        ssoSession.setAutheticatedCredentials(ssoSession.getTempCredentials());
        getSsoContext(request).registerSsoSession(ssoSession);

        ssoSession.setPrincipalRecord(principalRecord);
        ssoSession.setProfile(profileName);
        ssoSession.setStatus(SsoSession.Status.authenticated);
        ssoSession.setTempCredentials(null);
        ssoSession.setUserId(getUserId(principalRecord));
        try {
            ssoSession.setValidTo(validTo == null ?
                    Integer.MAX_VALUE : (int) new SimpleDateFormat("yyyyMMddHHmmssZ").parse(validTo).getTime());
        } catch (Exception ex) {
            log.error("Unable to parse Auth Record valid_to value: {}", ex.getMessage());
            log.debug("Exception", ex);
        }

        persistUserPassword(request, ssoSession, password);

        ssoSession.touch();
        return ssoSession;
    }

    public static void validateClientAcceptHeader(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        if (StringUtils.isEmpty(acceptHeader) || !acceptHeader.equals("application/json")) {
            throw new OAuthException(SsoConstants.ERR_CODE_INVALID_REQUEST,
                    String.format(SsoConstants.ERR_CODE_INVALID_REQUEST_MSG, "Accept Header"));
        }
    }

    public static void validateClientRequest(
            HttpServletRequest request,
            String clientId,
            String clientSecret,
            String scope,
            String redirectUri) {
        try {
            SsoContext ssoContext = getSsoContext(request);
            ClientInfo clientInfo = ssoContext.getClienInfo(clientId);
            if (clientInfo == null) {
                throw new OAuthException(SsoConstants.ERR_CODE_UNAUTHORIZED_CLIENT,
                        SsoConstants.ERR_CODE_UNAUTHORIZED_CLIENT_MSG);
            }
            if (!clientInfo.isTrusted()) {
                throw new OAuthException(SsoConstants.ERR_CODE_ACCESS_DENIED,
                        SsoConstants.ERR_CODE_ACCESS_DENIED_MSG);
            }
            if (StringUtils.isNotEmpty(clientSecret) && !EnvelopePBE.check(clientInfo.getClientSecret(), clientSecret)) {
                throw new OAuthException(SsoConstants.ERR_CODE_INVALID_REQUEST,
                        String.format(SsoConstants.ERR_CODE_INVALID_REQUEST_MSG, SsoConstants.HTTP_PARAM_CLIENT_SECRET));
            }
            if (StringUtils.isNotEmpty(scope)) {
                validateScope(clientInfo.getScope(), scope);
            }
            if (StringUtils.isNotEmpty(redirectUri) &&
                    ssoContext.getSsoLocalConfig().getBoolean("SSO_CALLBACK_PREFIX_CHECK")) {
                List<String> allowedPrefixes = scopeAsList(clientInfo.getCallbackPrefix());
                boolean isValidUri = false;
                for (String allowedPrefix : allowedPrefixes) {
                    if (redirectUri.toLowerCase().startsWith(allowedPrefix.toLowerCase())) {
                        isValidUri = true;
                        break;
                    }
                }
                if (!isValidUri) {
                    throw new OAuthException(SsoConstants.ERR_CODE_UNAUTHORIZED_CLIENT,
                            SsoConstants.ERR_CODE_UNAUTHORIZED_CLIENT_MSG);
                }
            }
        } catch (OAuthException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Internal Server Error: {}", ex.getMessage());
            log.debug("Exception", ex);
            throw new OAuthException(SsoConstants.ERR_CODE_SERVER_ERROR, ex.getMessage());
        }
    }

    public static void validateRequestScope(HttpServletRequest req, String token, String scope) {
        if (StringUtils.isNotEmpty(scope)) {
            SsoSession ssoSession = getSsoSessionFromRequest(req, token);
            if (ssoSession != null && ssoSession.getScope() != null) {
                validateScope(ssoSession.getScopeAsList(), scope);
            }
        }
    }

    public static void validateScope(List<String> scope, String requestScope) {
        List<String> requestedScope = strippedScopeAsList(scopeAsList(requestScope));
        if (!scope.containsAll(requestedScope)) {
            throw new OAuthException(SsoConstants.ERR_CODE_INVALID_SCOPE,
                    String.format(SsoConstants.ERR_CODE_INVALID_SCOPE_MSG, requestedScope));
        }
    }

    public static void sendJsonDataWithMessage(
            HttpServletResponse response,
            String errorCode,
            Exception ex) throws IOException {
        sendJsonDataWithMessage(response, new OAuthException(errorCode, ex.getMessage(), ex));
    }

    public static void sendJsonDataWithMessage(HttpServletResponse response, OAuthException ex) throws IOException {
        sendJsonDataWithMessage(response, ex, false);
    }

    public static void sendJsonDataWithMessage(
            HttpServletResponse response,
            OAuthException ex,
            boolean isValidateRequest) throws IOException {
        if (isValidateRequest) {
            log.debug("OAuthException {}: {}", ex.getCode(), ex.getMessage());
        } else {
            log.error("OAuthException {}: {}", ex.getCode(), ex.getMessage());
        }
        log.debug("Exception", ex);
        Map<String, Object> errorData = new HashMap<>();
        errorData.put(SsoConstants.ERROR_CODE, ex.getCode());
        errorData.put(SsoConstants.ERROR, ex.getMessage());
        sendJsonData(response, errorData);
    }

    public static void sendJsonData(HttpServletResponse response, Map<String, Object> payload) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            Map<String, Object> ovirtData = (Map<String, Object>) payload.get("ovirt");
            if (ovirtData != null) {
                Collection<ExtMap> groupIds = (Collection<ExtMap>) ovirtData.get("group_ids");
                if (groupIds != null) {
                    ovirtData.put("group_ids", SsoUtils.prepareGroupMembershipsForJson(groupIds));
                }
            }
            String jsonPayload = getJson(payload);
            response.setContentType("application/json");
            byte[] jsonPayloadBytes = jsonPayload.getBytes(StandardCharsets.UTF_8.name());
            response.setContentLength(jsonPayloadBytes.length);
            os.write(jsonPayloadBytes);
            log.trace("Sending json data {}", jsonPayload);
        }
    }

    public static List<String> strippedScopeAsList(List<String> scope) {
        List<String> scopes = new ArrayList<>();
        String[] tokens;
        for (String s : scope) {
            tokens = s.split("=", 3);
            if (tokens.length == 1) {
                scopes.add(tokens[0]);
            } else if (!tokens[1].equals("auth:identity")) {
                scopes.add(tokens[0] + "=" + tokens[1]);
            }
        }
        return scopes;
    }

    public static List<String> scopeAsList(String scope) {
        return StringUtils.isEmpty(scope) ? Collections.<String>emptyList() : Arrays.asList(scope.trim().split("\\s *"));
    }

    public static String encrypt(ServletContext ctx, String clientId, String rawText) throws Exception {
        ClientInfo clientInfo = getSsoContext(ctx).getClienInfo(clientId);
        try (InputStream in = new FileInputStream(clientInfo.getCertificateLocation())) {
            return EnvelopeEncryptDecrypt.encrypt(
                    "AES/OFB/PKCS5Padding",
                    256,
                    CertificateFactory.getInstance("X.509").generateCertificate(in),
                    100,
                    rawText.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void notifyClientsOfLogoutEvent(
            SsoContext ssoContext,
            Set<String> clientIdsForToken,
            String token) throws Exception {
        if (clientIdsForToken != null) {
            for (String clientId : clientIdsForToken) {
                notifyClientOfLogoutEvent(ssoContext, clientId, token);
            }
        }
    }

    private static void notifyClientOfLogoutEvent(
            SsoContext ssoContext,
            String clientId,
            String token) throws Exception {
        ClientInfo clientInfo = ssoContext.getClienInfo(clientId);
        String url = clientInfo.getClientNotificationCallback();
        if (StringUtils.isNotEmpty(url)) {
            HttpURLConnection connection = null;
            try {
                connection = createConnection(ssoContext.getSsoLocalConfig(), clientInfo, url);
                String data = new URLBuilder(url).addParameter("event", "logout")
                        .addParameter("token", token)
                        .addParameter("token_type", "bearer").buildURL().getQuery();
                connection.setRequestProperty("Content-Length", "" + data.length());
                connection.connect();
                try (OutputStreamWriter outputWriter = new OutputStreamWriter(connection.getOutputStream())) {
                    outputWriter.write(data);
                    outputWriter.flush();
                }

                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    final byte[] buffer = new byte[8*1024];
                    int n;
                    try (InputStream input = connection.getInputStream()) {
                        while ((n = input.read(buffer)) != -1) {
                            os.write(buffer, 0, n);
                        }
                    }
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    private static HttpURLConnection createConnection(
            SsoLocalConfig config,
            ClientInfo clientInfo,
            String url) throws Exception {
        HttpURLConnection connection = new HttpURLConnectionBuilder(url)
                .setHttpsProtocol(clientInfo.getNotificationCallbackProtocol())
                .setReadTimeout(config.getInteger("SSO_CALLBACK_READ_TIMEOUT"))
                .setConnectTimeout(config.getInteger("SSO_CALLBACK_CONNECT_TIMEOUT"))
                .setTrustManagerAlgorithm(TrustManagerFactory.getDefaultAlgorithm())
                .setTrustStore(config.getProperty("ENGINE_HTTPS_PKI_TRUST_STORE"))
                .setTrustStorePassword(config.getProperty("ENGINE_HTTPS_PKI_TRUST_STORE_PASSWORD"))
                .setTrustStoreType(config.getProperty("ENGINE_HTTPS_PKI_TRUST_STORE_TYPE"))
                .setURL(url)
                .setVerifyChain(clientInfo.isNotificationCallbackVerifyChain())
                .setVerifyHost(clientInfo.isNotificationCallbackVerifyHost()).create();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty(HttpHeaders.ACCEPT, "application/json");
        connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        return connection;
    }

    /**
     * Currently jackson doesn't provide a way how to serialize graphs with cyclic references between nodes, which
     * may happen if those cyclic dependencies exists among nested groups which is a user member of. So in order to
     * serialize to JSON successfully we do the following:
     *   1. If a principal is a direct member of a group, than put into group record key
     *      {@code Authz.PrincipalRecord.PRINCIPAL}
     *   2. Change group memberships to contain only IDs of groups and not full group records by changing list in
     *      {@code Authz.GroupRecord.GROUPS} from {@code Collection<ExtMap>} to {@code Collection<String>}
     *   3. Return all referenced group records as a set
     * The whole process needs to be reversed on engine side, see
     * {@code org.ovirt.engine.core.aaa.SsoOAuthServiceUtils.processGroupMembershipsFromJson()}
     */
    public static Collection<ExtMap> prepareGroupMembershipsForJson(Collection<ExtMap> groupRecords) {
        Map<String, ExtMap> resolvedGroups = new HashMap<>();
        for (ExtMap origRecord : groupRecords) {
            if (!resolvedGroups.containsKey(origRecord.<String>get(Authz.GroupRecord.ID))) {
                ExtMap groupRecord = new ExtMap(origRecord);
                groupRecord.put(Authz.PrincipalRecord.PRINCIPAL, "");
                resolvedGroups.put(groupRecord.<String>get(Authz.GroupRecord.ID), groupRecord);
                groupRecord.put(
                        Authz.GroupRecord.GROUPS,
                        processGroupMemberships(
                                groupRecord.<Collection<ExtMap>>get(
                                        Authz.GroupRecord.GROUPS,
                                        Collections.<ExtMap>emptyList()),
                                resolvedGroups
                        ));
            }
        }
        return new ArrayList<>(resolvedGroups.values());
    }

    private static Set<String> processGroupMemberships(
            Collection<ExtMap> memberships,
            Map<String, ExtMap> resolvedGroups) {
        Set<String> membershipIds = new HashSet<>();
        for (ExtMap origRecord : memberships) {
            ExtMap groupRecord = new ExtMap(origRecord);
            membershipIds.add(groupRecord.<String>get(Authz.GroupRecord.ID));
            if (!resolvedGroups.containsKey(groupRecord.<String>get(Authz.GroupRecord.ID))) {
                resolvedGroups.put(groupRecord.<String>get(Authz.GroupRecord.ID), groupRecord);
                groupRecord.put(
                        Authz.GroupRecord.GROUPS,
                        processGroupMemberships(
                                groupRecord.<Collection<ExtMap>>get(
                                        Authz.GroupRecord.GROUPS,
                                        Collections.<ExtMap>emptyList()),
                                resolvedGroups
                        ));
            }
        }
        return membershipIds;
    }
}
