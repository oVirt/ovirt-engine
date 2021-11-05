package org.ovirt.engine.core.aaa;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.net.ssl.TrustManagerFactory;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.aaa.filters.FiltersHelper;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.serialization.json.JsonExtMapMixIn;
import org.ovirt.engine.core.uutils.IOUtils;
import org.ovirt.engine.core.uutils.net.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SsoOAuthServiceUtils {
    private static final Logger log = LoggerFactory.getLogger(SsoOAuthServiceUtils.class);

    private static final String authzSearchScope = "ovirt-ext=token-info:authz-search";
    private static final String publicAuthzSearchScope = "ovirt-ext=token-info:public-authz-search";

    // Reference to the HTTP client used to send the requests to the SSO server:
    private static volatile CloseableHttpClient client;
    private static final ObjectMapper mapper;

    static {
        // Remember to close the client when going down:
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(() -> IOUtils.closeQuietly(client)));
        mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator())
                .addMixIn(ExtMap.class, JsonExtMapMixIn.class);
    }

    public static Map<String, Object> authenticate(HttpServletRequest req, String scope) {
        try {
            HttpPost request = createPost("/oauth/token");
            setClientIdSecretBasicAuthHeader(request);
            String[] credentials = getUserCredentialsFromHeader(req);
            List<BasicNameValuePair> form = new ArrayList<>(4);
            form.add(new BasicNameValuePair("grant_type", "password"));
            form.add(new BasicNameValuePair("username", credentials[0]));
            form.add(new BasicNameValuePair("password", credentials[1]));
            form.add(new BasicNameValuePair("scope", scope));
            request.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
            return getResponse(request);
        } catch (Exception ex) {
            return buildMapWithError("server_error", ex.getMessage());
        }
    }

    public static Map<String, Object> loginOnBehalf(HttpServletRequest req,
            String username,
            String scope,
            ExtMap authRecord) {
        return loginWithPasswordImpl(username, "", scope, authRecord, getParams(req));
    }

    private static Map<String, String> getParams(HttpServletRequest req) {
        Map<String, String> params = null;
        if (EngineLocalConfig.getInstance().getBoolean("ENGINE_SSO_ENABLE_EXTERNAL_SSO")) {
            params = new HashMap<>();
            Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                params.put(headerName, req.getHeader(headerName));
            }
        }
        return params;
    }

    public static Map<String, Object> loginWithPassword(String username, String password, String scope) {
        return loginWithPasswordImpl(username, password, scope, null, null);
    }

    private static Map<String, Object> loginWithPasswordImpl(
            String username,
            String password,
            String scope,
            ExtMap authRecord,
            Map<String, String> params) {
        try {
            HttpPost request = createPost("/oauth/token");
            setClientIdSecretBasicAuthHeader(request);
            List<BasicNameValuePair> form = new ArrayList<>(5);
            form.add(new BasicNameValuePair("grant_type", "password"));
            form.add(new BasicNameValuePair("username", username));
            form.add(new BasicNameValuePair("password", password));
            form.add(new BasicNameValuePair("scope", scope));
            if (authRecord != null) {
                form.add(new BasicNameValuePair("ovirt_auth_record", serialize(authRecord)));
            }
            if (params != null) {
                form.add(new BasicNameValuePair("params", serialize(params)));
            }
            request.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
            return getResponse(request);
        } catch (Exception ex) {
            return buildMapWithError("server_error", ex.getMessage());
        }
    }

    private static String serialize(Object obj) throws IOException {
        return mapper.writeValueAsString(obj);
    }

    private static <T> T deserialize(String json, Class<T> type) throws IOException {
        return mapper.readValue(json, type);
    }

    public static Map<String, Object> revoke(String token) {
        return revoke(token, "ovirt-ext=revoke:revoke-all");
    }

    public static Map<String, Object> revoke(String token, String scope) {
        try {
            HttpPost request = createPost("/oauth/revoke");
            setClientIdSecretBasicAuthHeader(request);
            List<BasicNameValuePair> form = new ArrayList<>(2);
            form.add(new BasicNameValuePair("token", token));
            form.add(new BasicNameValuePair("scope", scope));
            request.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
            return getResponse(request);
        } catch (Exception ex) {
            return buildMapWithError("server_error", ex.getMessage());
        }
    }

    public static Map<String, Object> getToken(String grantType, String code, String scope, String redirectUri) {
        try {
            HttpPost request = createPost("/oauth/token");
            setClientIdSecretBasicAuthHeader(request);
            List<BasicNameValuePair> form = new ArrayList<>(4);
            form.add(new BasicNameValuePair("grant_type", grantType));
            form.add(new BasicNameValuePair("code", code));
            form.add(new BasicNameValuePair("redirect_uri", redirectUri));
            form.add(new BasicNameValuePair("scope", scope));
            request.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
            return getResponse(request);
        } catch (Exception ex) {
            return buildMapWithError("server_error", ex.getMessage());
        }
    }

    public static Map<String, Object> getTokenInfo(String token) {
        return getTokenInfo(token, null);
    }

    public static Map<String, Object> getTokenInfo(String token, String scope) {
        try {
            HttpPost request = createPost("/oauth/token-info");
            setClientIdSecretBasicAuthHeader(request);
            List<BasicNameValuePair> form = new ArrayList<>(2);
            form.add(new BasicNameValuePair("token", token));
            if (StringUtils.isNotEmpty(scope)) {
                form.add(new BasicNameValuePair("scope", scope));
            }
            request.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
            Map<String, Object> jsonData = getResponse(request);
            Map<String, Object> ovirtData = (Map<String, Object>) jsonData.get("ovirt");
            if (ovirtData != null) {
                Collection<ExtMap> groupIds = (Collection<ExtMap>) ovirtData.get("group_ids");
                if (groupIds != null) {
                    ovirtData.put("group_ids", SsoOAuthServiceUtils.processGroupMembershipsFromJson(groupIds));
                }
            }
            return jsonData;
        } catch (Exception ex) {
            return buildMapWithError("server_error", ex.getMessage());
        }
    }

    public static Map<String, Object> isSsoDeployed() {
        try {
            HttpGet request = createGet("/status");
            return getResponse(request);
        } catch (FileNotFoundException ex) {
            return buildMapWithError("server_error", "oVirt Engine is initializing.");
        } catch (Exception ex) {
            return buildMapWithError("server_error", ex.getMessage());
        }
    }

    public static Map<String, Object> fetchPrincipalRecord(
            String token,
            String domain,
            String principal,
            boolean groupsResolving,
            boolean groupsResolvingRecursive) {
        Map<String, Object> params = new HashMap<>();
        params.put("domain", domain);
        params.put("principal", principal);
        params.put("groups_resolving", groupsResolving);
        params.put("groups_resolving_recursive", groupsResolvingRecursive);
        return search(token, params, "fetch-principal-record", authzSearchScope);

    }

    public static Map<String, Object> findPrincipalsByIds(
            String token,
            String domain,
            String namespace,
            Collection<String> ids,
            boolean groupsResolving,
            boolean groupsResolvingRecursive) {
        Map<String, Object> params = new HashMap<>();
        params.put("domain", domain);
        params.put("namespace", namespace);
        params.put("ids", ids);
        params.put("groups_resolving", groupsResolving);
        params.put("groups_resolving_recursive", groupsResolvingRecursive);
        return search(token, params, "find-principals-by-ids", authzSearchScope);
    }

    public static Map<String, Object> findLoginOnBehalfPrincipalById(
            String domain,
            String namespace,
            Collection<String> ids,
            boolean groupsResolving,
            boolean groupsResolvingRecursive) {
        Map<String, Object> params = new HashMap<>();
        params.put("domain", domain);
        params.put("namespace", namespace);
        params.put("ids", ids);
        params.put("groups_resolving", groupsResolving);
        params.put("groups_resolving_recursive", groupsResolvingRecursive);
        return search(null, params, "find-login-on-behalf-principal-by-id", publicAuthzSearchScope);
    }

    public static Map<String, Object> findDirectoryUserById(
            String token,
            String domain,
            String namespace,
            String id,
            boolean groupsResolving,
            boolean groupsResolvingRecursive) {
        Map<String, Object> params = new HashMap<>();
        params.put("domain", domain);
        params.put("namespace", StringUtils.defaultIfEmpty(namespace, ""));
        params.put("id", id);
        params.put("groups_resolving", groupsResolving);
        params.put("groups_resolving_recursive", groupsResolvingRecursive);
        return search(token, params, "find-principal-by-id", authzSearchScope);
    }

    public static Map<String, Object> findDirectoryGroupById(
            String token,
            String domain,
            String namespace,
            String id,
            boolean groupsResolving,
            boolean groupsResolvingRecursive) {
        Map<String, Object> params = new HashMap<>();
        params.put("domain", domain);
        params.put("namespace", StringUtils.defaultIfEmpty(namespace, ""));
        params.put("id", id);
        params.put("groups_resolving", groupsResolving);
        params.put("groups_resolving_recursive", groupsResolvingRecursive);
        return search(token, params, "find-directory-group-by-id", authzSearchScope);
    }

    public static Map<String, Object> getDomainList(String token) {
        return search(token, null, "domain-list", authzSearchScope);
    }

    public static Map<String, Object> getAvailableNamespaces(String token) {
        return search(token, null, "available-namespaces", authzSearchScope);
    }

    public static Map<String, Object> getSessionStatues(Set<String> entries) {
        return search(null, Collections.singletonMap("tokens", entries), "session-statuses", publicAuthzSearchScope);
    }

    public static Map<String, Object> getProfileList() {
        return search(null, null, "profile-list", publicAuthzSearchScope);
    }

    public static Map<String, Object> searchUsers(String token, Map<String, Object> params) {
        return search(token, params, "users", authzSearchScope);
    }

    public static Map<String, Object> searchGroups(String token, Map<String, Object> params) {
        return search(token, params, "groups", authzSearchScope);
    }

    private static Map<String, Object> search(
            String token,
            Map<String, Object> params,
            String queryType,
            String scope) {
        try {
            HttpPost request = createPost("/oauth/token-info");
            setClientIdSecretBasicAuthHeader(request);
            List<BasicNameValuePair> form = new ArrayList<>(4);
            form.add(new BasicNameValuePair("query_type", queryType));
            form.add(new BasicNameValuePair("scope", scope));
            if (StringUtils.isNotEmpty(token)) {
                form.add(new BasicNameValuePair("token", token));
            }
            if (params != null) {
                form.add(new BasicNameValuePair("params", serialize(params)));
            }
            request.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
            return getResponse(request);
        } catch (Exception ex) {
            return buildMapWithError("server_error", ex.getMessage());
        }
    }

    private static String[] getUserCredentialsFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String userName = "";
        String passwd = "";
        if (StringUtils.isNotEmpty(header) && header.startsWith("Basic")) {
            String[] creds = new String(
                    Base64.decodeBase64(header.substring("Basic".length())),
                    StandardCharsets.UTF_8
            ).split(":", 2);
            userName = creds.length >= 1 ? creds[0] : "";
            passwd = creds.length >= 2 ? creds[1] : "";
        }
        return new String[] {userName, passwd};
    }

    private static void setClientIdSecretBasicAuthHeader(HttpUriRequest request) {
        EngineLocalConfig config = EngineLocalConfig.getInstance();
        byte[] encodedBytes = Base64.encodeBase64(String.format("%s:%s",
                config.getProperty("ENGINE_SSO_CLIENT_ID"),
                config.getProperty("ENGINE_SSO_CLIENT_SECRET")).getBytes());
        request.setHeader(FiltersHelper.Constants.HEADER_AUTHORIZATION, String.format("Basic %s", new String(encodedBytes)));
    }

    private static Map<String, Object> buildMapWithError(String error, String error_description) {
        Map<String, Object> response = new HashMap<>();
        response.put("error_description", error_description);
        response.put("error", error);
        return response;
    }

    private static HttpPost createPost(String path) throws Exception {
        EngineLocalConfig config = EngineLocalConfig.getInstance();
        String base = config.getProperty("ENGINE_SSO_SERVICE_URL");
        HttpPost request = new HttpPost();
        request.setURI(new URI(base + path));
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-Language", "en-US");
        return request;
    }

    private static HttpGet createGet(String path) throws Exception {
        EngineLocalConfig config = EngineLocalConfig.getInstance();
        String base = config.getProperty("ENGINE_SSO_SERVICE_URL");
        HttpGet request = new HttpGet();
        request.setURI(new URI(base + path));
        request.setHeader("Accept", "application/json");
        return request;
    }

    private static Map<String, Object> getResponse(HttpUriRequest request) throws Exception {
        try (CloseableHttpResponse response = execute(request)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                throw new FileNotFoundException();
            }
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                try (InputStream input = response.getEntity().getContent()) {
                    FiltersHelper.copy(input, os);
                }
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(SsoOAuthServiceUtils.class.getClassLoader());
                try {
                    return deserialize(
                            new String(os.toByteArray(), StandardCharsets.UTF_8.name()),
                            HashMap.class);
                } finally {
                    Thread.currentThread().setContextClassLoader(loader);
                }
            }
        }
    }

    private static CloseableHttpResponse execute(HttpUriRequest request) throws IOException, GeneralSecurityException {
        // Make sure the client is created:
        if (client == null) {
            synchronized (SsoOAuthServiceUtils.class) {
                if (client == null) {
                    client = createClient();
                }
            }
        }

        // Execute the request:
        return client.execute(request);
    }

    private static CloseableHttpClient createClient() throws IOException, GeneralSecurityException {
        EngineLocalConfig config = EngineLocalConfig.getInstance();
        return new HttpClientBuilder()
                .setSslProtocol(config.getProperty("ENGINE_SSO_SERVICE_SSL_PROTOCOL"))
                .setPoolSize(config.getInteger("ENGINE_SSO_SERVICE_CLIENT_POOL_SIZE"))
                .setReadTimeout(0)
                .setRetryCount(config.getInteger("ENGINE_SSO_SERVICE_CONNECTION_RETRY_COUNT"))
                .setTrustManagerAlgorithm(TrustManagerFactory.getDefaultAlgorithm())
                .setTrustStore(config.getProperty("ENGINE_HTTPS_PKI_TRUST_STORE"))
                .setTrustStorePassword(config.getProperty("ENGINE_HTTPS_PKI_TRUST_STORE_PASSWORD"))
                .setTrustStoreType(config.getProperty("ENGINE_HTTPS_PKI_TRUST_STORE_TYPE"))
                .setValidateAfterInactivity(config.getInteger("ENGINE_SSO_SERVICE_CONNECTION_VALIDATE_AFTER_INACTIVITY"))
                .setVerifyChain(config.getBoolean("ENGINE_SSO_SERVICE_SSL_VERIFY_CHAIN"))
                .setVerifyHost(config.getBoolean("ENGINE_SSO_SERVICE_SSL_VERIFY_HOST"))
                .build();
    }

    /**
     * Currently jackson doesn't provide a way how to serialize graphs with cyclic references between nodes, which
     * may happen if those cyclic dependencies exists among nested groups which is a user member of. So in order to
     * deserialize from JSON successfully we have to revert changes done in
     * {@code org.ovirt.engine.core.sso.utils.SsoUtils.prepareGroupMembershipsForJson()}
     */
    public static List<ExtMap> processGroupMembershipsFromJson(Collection<ExtMap> jsonGroupMemberships) {
        Map<String, ExtMap> groupsCache = jsonGroupMemberships.stream()
                .collect(toMap(item -> item.get(Authz.GroupRecord.ID), Function.identity()));
        jsonGroupMemberships.forEach(groupRecord -> groupRecord.put(
                Authz.GroupRecord.GROUPS,
                groupRecord.<Collection<String>>get(Authz.GroupRecord.GROUPS, Collections.emptyList()).stream()
                        .map(memberOfId -> groupsCache.get(memberOfId))
                        .collect(toList())));
        return groupsCache.values().stream()
                .filter(group -> group.containsKey(Authz.PrincipalRecord.PRINCIPAL))
                .peek(group -> group.remove(Authz.PrincipalRecord.PRINCIPAL))
                .collect(toList());
    }
}
