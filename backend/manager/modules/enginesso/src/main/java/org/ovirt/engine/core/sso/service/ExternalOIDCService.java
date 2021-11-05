package org.ovirt.engine.core.sso.service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.TrustManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.sso.api.AuthResult;
import org.ovirt.engine.core.sso.api.AuthenticationException;
import org.ovirt.engine.core.sso.api.Credentials;
import org.ovirt.engine.core.sso.api.NonInteractiveAuth;
import org.ovirt.engine.core.sso.api.OAuthException;
import org.ovirt.engine.core.sso.api.SsoConstants;
import org.ovirt.engine.core.sso.api.SsoContext;
import org.ovirt.engine.core.sso.api.SsoSession;
import org.ovirt.engine.core.sso.utils.SsoLocalConfig;
import org.ovirt.engine.core.uutils.IOUtils;
import org.ovirt.engine.core.uutils.net.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExternalOIDCService {

    private static Logger log = LoggerFactory.getLogger(ExternalOIDCService.class);

    // Reference to the HTTP client used to send the requests to the SSO server:
    private static volatile CloseableHttpClient client;

    private static final ObjectMapper mapper;

    static {
        // Remember to close the client when going down:
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(() -> IOUtils.closeQuietly(client)));
        mapper = new ObjectMapper();
    }

    public static void issueTokenUsingExternalOIDC(SsoContext ssoContext,
            HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {
        log.debug("Entered issueTokenUsingExternalOIDC");
        try {
            AuthResult authResult = NonInteractiveAuth.OIDC.doAuth(request, response);
            if (authResult != null && StringUtils.isNotEmpty(authResult.getToken())) {
                SsoSession ssoSession = SsoService.getSsoSessionFromRequest(request, authResult.getToken());
                if (ssoSession == null) {
                    throw new OAuthException(SsoConstants.ERR_CODE_INVALID_GRANT,
                            ssoContext.getLocalizationUtils()
                                    .localize(
                                            SsoConstants.APP_ERROR_AUTHORIZATION_GRANT_EXPIRED,
                                            (Locale) request.getAttribute(SsoConstants.LOCALE)));
                }
                SsoService.validateClientAcceptHeader(request);
                log.debug("Sending json response");
                SsoService.sendJsonData(response, buildResponse(ssoSession));
            } else {
                throw new AuthenticationException(
                        ssoContext.getLocalizationUtils()
                                .localize(
                                        SsoConstants.APP_ERROR_AUTHENTICATION_FAILED,
                                        (Locale) request.getAttribute(SsoConstants.LOCALE)));
            }
        } catch (Exception ex) {
            throw new AuthenticationException(
                    String.format(
                            ssoContext.getLocalizationUtils()
                                    .localize(
                                            SsoConstants.APP_ERROR_CANNOT_AUTHENTICATE_USER,
                                            (Locale) request.getAttribute(SsoConstants.LOCALE)),
                            ex.getMessage()));
        }
    }

    public static void handleCredentials(
            SsoContext ssoContext,
            HttpServletRequest request,
            Credentials credentials) throws Exception {

        SsoSession ssoSession = login(ssoContext, request, credentials);
        log.info("User {}@{} with profile [{}] successfully logged into external OP with scopes: {}",
                SsoService.getUserId(ssoSession.getPrincipalRecord()),
                ssoContext.getUserAuthzName(ssoSession),
                ssoSession.getProfile(),
                ssoSession.getScope());
    }

    private static SsoSession login(SsoContext ssoContext,
            HttpServletRequest request,
            Credentials credentials) throws Exception {
        String externalOidcTokenEndPoint = ssoContext.getSsoLocalConfig().getProperty("EXTERNAL_OIDC_TOKEN_END_POINT");
        String externalOidcClientId = ssoContext.getSsoLocalConfig().getProperty("EXTERNAL_OIDC_CLIENT_ID");
        String externalOidcClientSecret = ssoContext.getSsoLocalConfig().getProperty("EXTERNAL_OIDC_CLIENT_SECRET");
        String scope = SsoService.getScopeRequestParameter(request, "");

        HttpPost post = createPost(externalOidcTokenEndPoint);
        List<BasicNameValuePair> form = new ArrayList<>();
        form.add(new BasicNameValuePair("client_id", externalOidcClientId));
        form.add(new BasicNameValuePair("client_secret", externalOidcClientSecret));
        form.add(new BasicNameValuePair("username", credentials.getUsername()));
        form.add(new BasicNameValuePair("password", credentials.getPassword()));
        form.add(new BasicNameValuePair("grant_type", "password"));
        form.add(new BasicNameValuePair("scope", scope));
        post.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
        Map<String, Object> response = getResponse(ssoContext, post);

        if (response.containsKey("error") && response.containsKey("error_description")) {
            throw new OAuthException((String) response.get("error"), (String) response.get("error_description"));
        }

        if (!response.containsKey("access_token")) {
            throw new OAuthException(SsoConstants.ERR_CODE_SERVER_ERROR, "Unable to get token from external OIDC.");
        }

        String accessToken = (String) response.get("access_token");
        request.setAttribute(SsoConstants.HTTP_REQ_ATTR_ACCESS_TOKEN, accessToken);

        SsoSession ssoSession = SsoService.persistAuthInfoInContextWithToken(request,
                accessToken,
                credentials.getPassword(),
                credentials.getProfile(),
                buildAuthRecord(credentials.getUsername()),
                buildPrincipalRecord(getUserInfo(ssoContext, accessToken), credentials.getUsername()));

        if (response.containsKey("refresh_token")) {
            ssoSession.setRefreshToken((String) response.get("refresh_token"));
        }
        return ssoSession;
    }

    private static Map<String, Object> getUserInfo(SsoContext ssoContext, String token) throws Exception {
        HttpPost post = createPost(ssoContext.getSsoLocalConfig().getProperty("EXTERNAL_OIDC_USER_INFO_END_POINT"));
        List<BasicNameValuePair> form = new ArrayList<>();
        form.add(new BasicNameValuePair("access_token", token));
        post.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));

        Map<String, Object> response = getResponse(ssoContext, post);
        if (response.containsKey("error") && response.containsKey("error_description")) {
            throw new OAuthException((String) response.get("error"), (String) response.get("error_description"));
        }

        return response;
    }

    private static ExtMap buildAuthRecord(String userName) {
        ExtMap authRecord = new ExtMap();
        authRecord.put(Authn.AuthRecord.PRINCIPAL, userName);
        return authRecord;
    }

    private static ExtMap buildPrincipalRecord(Map<String, Object> response, String userName) {
        ExtMap principalRecord = new ExtMap();
        principalRecord.mput(
                Authz.PrincipalRecord.NAMESPACE,
                "*")
                .mput(
                        Authz.PrincipalRecord.PRINCIPAL,
                        userName)
                .mput(
                        Authz.PrincipalRecord.NAME,
                        userName)
                .mput(
                        Authz.PrincipalRecord.LAST_NAME,
                        "")
                .mput(
                        Authz.PrincipalRecord.GROUPS,
                        buildPrincipalRecordGroups(response));
        return principalRecord;
    }

    private static Collection<ExtMap> buildPrincipalRecordGroups(Map<String, Object> response) {
        LinkedList<ExtMap> groups = new LinkedList<>();
        List<String> groupNames = (List<String>) response.get("groups");
        for (String groupName : groupNames) {
            groupName = groupName.replaceFirst("^/", "");
            ExtMap group = new ExtMap();
            group.mput(
                    Authz.GroupRecord.GROUPS,
                    new LinkedList<>())
                    .mput(
                            Authz.GroupRecord.NAMESPACE,
                            "*")
                    .mput(
                            Authz.GroupRecord.NAME,
                            groupName)
                    .mput(
                            Authz.GroupRecord.ID,
                            groupName);
            groups.add(group);
        }
        return groups;
    }

    private static HttpPost createPost(String url) throws Exception {
        HttpPost request = new HttpPost();
        request.setURI(new URI(url));
        request.setHeader("Accept", "application/json");
        return request;
    }

    private static Map<String, Object> getResponse(SsoContext ssoContext, HttpUriRequest request) throws Exception {
        try (CloseableHttpResponse response = execute(ssoContext, request)) {
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                throw new FileNotFoundException();
            }

            if (statusCode == HttpStatus.SC_NO_CONTENT) {
                return new HashMap<>();
            }

            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                try (InputStream input = response.getEntity().getContent()) {
                    copy(input, os);
                }

                return deserialize(new String(os.toByteArray(), StandardCharsets.UTF_8.name()));
            }
        }
    }

    private static long copy(final InputStream input, final OutputStream output) throws IOException {
        final byte[] buffer = new byte[8 * 1024];
        long count = 0;
        int n;
        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    private static Map<String, Object> deserialize(String json) throws IOException {
        return mapper.readValue(json, new TypeReference<>() {
        });
    }

    private static CloseableHttpResponse execute(SsoContext ssoContext, HttpUriRequest request)
            throws IOException, GeneralSecurityException {
        // Make sure the client is created:
        if (client == null) {
            synchronized (ExternalOIDCService.class) {
                if (client == null) {
                    client = createClient(ssoContext);
                }
            }
        }

        // Execute the request:
        return client.execute(request);
    }

    private static CloseableHttpClient createClient(SsoContext ssoContext)
            throws IOException, GeneralSecurityException {
        SsoLocalConfig config = ssoContext.getSsoLocalConfig();
        return new HttpClientBuilder()
                .setSslProtocol(config.getProperty("EXTERNAL_OIDC_SSL_PROTOCOL"))
                .setPoolSize(config.getInteger("EXTERNAL_OIDC_CLIENT_POOL_SIZE"))
                .setReadTimeout(config.getInteger("EXTERNAL_OIDC_READ_TIMEOUT"))
                .setConnectTimeout(config.getInteger("EXTERNAL_OIDC_CONNECT_TIMEOUT"))
                .setRetryCount(config.getInteger("EXTERNAL_OIDC_CONNECTION_RETRY_COUNT"))
                .setTrustManagerAlgorithm(TrustManagerFactory.getDefaultAlgorithm())
                .setTrustStore(config.getProperty("EXTERNAL_OIDC_HTTPS_PKI_TRUST_STORE"))
                .setTrustStorePassword(config.getProperty("EXTERNAL_OIDC_HTTPS_PKI_TRUST_STORE_PASSWORD"))
                .setTrustStoreType(config.getProperty("EXTERNAL_OIDC_HTTPS_PKI_TRUST_STORE_TYPE"))
                .setValidateAfterInactivity(config.getInteger("EXTERNAL_OIDC_CONNECTION_VALIDATE_AFTER_INACTIVITY"))
                .setVerifyChain(config.getBoolean("EXTERNAL_OIDC_SSL_VERIFY_CHAIN"))
                .setVerifyHost(config.getBoolean("EXTERNAL_OIDC_SSL_VERIFY_HOST"))
                .build();
    }

    private static Map<String, Object> buildResponse(SsoSession ssoSession) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(SsoConstants.JSON_ACCESS_TOKEN, ssoSession.getAccessToken());
        payload.put(SsoConstants.JSON_SCOPE, StringUtils.isEmpty(ssoSession.getScope()) ? "" : ssoSession.getScope());
        payload.put(SsoConstants.JSON_EXPIRES_IN, ssoSession.getValidTo().toString());
        payload.put(SsoConstants.JSON_TOKEN_TYPE, "bearer");
        return payload;
    }

    public static void logout(SsoContext ssoContext, String refreshToken) {
        try {
            String externalOidcClientId = ssoContext.getSsoLocalConfig().getProperty("EXTERNAL_OIDC_CLIENT_ID");
            String externalOidcClientSecret = ssoContext.getSsoLocalConfig().getProperty("EXTERNAL_OIDC_CLIENT_SECRET");

            HttpPost post = createPost(ssoContext.getSsoLocalConfig().getProperty("EXTERNAL_OIDC_LOGOUT_END_POINT"));
            List<BasicNameValuePair> form = new ArrayList<>();
            form.add(new BasicNameValuePair("refresh_token", refreshToken));
            form.add(new BasicNameValuePair("client_id", externalOidcClientId));
            form.add(new BasicNameValuePair("client_secret", externalOidcClientSecret));
            post.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));

            Map<String, Object> response = getResponse(ssoContext, post);
            if (response.containsKey("error") && response.containsKey("error_description")) {
                throw new OAuthException((String) response.get("error"), (String) response.get("error_description"));
            }
        } catch (Throwable t) {
            log.error("Unable to logout of external OIDC {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }
}
