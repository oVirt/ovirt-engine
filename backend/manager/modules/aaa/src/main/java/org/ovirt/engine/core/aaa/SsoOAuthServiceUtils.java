package org.ovirt.engine.core.aaa;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.net.ssl.TrustManagerFactory;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.aaa.filters.FiltersHelper;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectSerializer;
import org.ovirt.engine.core.uutils.net.HttpURLConnectionBuilder;
import org.ovirt.engine.core.uutils.net.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoOAuthServiceUtils {
    private static final Logger log = LoggerFactory.getLogger(SsoOAuthServiceUtils.class);

    private static final String authzSearchScope = "ovirt-ext=token-info:authz-search";
    private static final String publicAuthzSearchScope = "ovirt-ext=token-info:public-authz-search";

    public static Map<String, Object> authenticate(HttpServletRequest req, String scope) {
        HttpURLConnection connection = null;
        try {
            connection = createConnection("/oauth/token");
            setClientIdSecretBasicAuthHeader(connection);
            String[] credentials = getUserCredentialsFromHeader(req);
            postData(connection, new URLBuilder(connection.getURL()).addParameter("grant_type", "password")
                    .addParameter("username", credentials[0])
                    .addParameter("password", credentials[1])
                    .addParameter("scope", scope).buildURL().getQuery());
            return getData(connection);
        } catch (Exception ex) {
            return buildMapWithError("server_error", ex.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    public static Map<String, Object> loginOnBehalf(String username, String scope, ExtMap authRecord) {
        return loginWithPasswordImpl(username, "", scope, authRecord);
    }

    public static Map<String, Object> loginWithPassword(String username, String password, String scope) {
        return loginWithPasswordImpl(username, password, scope, null);
    }

    private static Map<String, Object> loginWithPasswordImpl(
            String username,
            String password,
            String scope,
            ExtMap authRecord) {
        HttpURLConnection connection = null;
        try {
            connection = createConnection("/oauth/token");
            setClientIdSecretBasicAuthHeader(connection);
            URLBuilder urlBuilder = new URLBuilder(connection.getURL()).addParameter("grant_type", "password")
                    .addParameter("username", username)
                    .addParameter("password", password)
                    .addParameter("scope", scope);
            if (authRecord != null) {
                urlBuilder.addParameter("ovirt_auth_record", new JsonObjectSerializer().serialize(authRecord));
            }
            postData(connection, urlBuilder.buildURL().getQuery());
            return getData(connection);
        } catch (Exception ex) {
            return buildMapWithError("server_error", ex.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static Map<String, Object> revoke(String token) {
        return revoke(token, "ovirt-ext=revoke:revoke-all");
    }

    public static Map<String, Object> revoke(String token, String scope) {
        HttpURLConnection connection = null;
        try {
            connection = createConnection("/oauth/revoke");
            setClientIdSecretBasicAuthHeader(connection);
            postData(connection, new URLBuilder(connection.getURL()).addParameter("token", token)
                    .addParameter("scope", scope).buildURL().getQuery());
            return getData(connection);
        } catch (Exception ex) {
            return buildMapWithError("server_error", ex.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    public static Map<String, Object> getToken(String grantType, String code, String scope, String redirectUri) {
        HttpURLConnection connection = null;
        try {
            connection = createConnection("/oauth/token");
            setClientIdSecretBasicAuthHeader(connection);
            postData(connection, new URLBuilder(connection.getURL()).addParameter("grant_type", grantType)
                    .addParameter("code", code)
                    .addParameter("redirect_uri", redirectUri)
                    .addParameter("scope", scope).buildURL().getQuery());
            return getData(connection);
        } catch (Exception ex) {
            return buildMapWithError("server_error", ex.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    public static Map<String, Object> getTokenInfo(String token) {
        return getTokenInfo(token, null);
    }

    public static Map<String, Object> getTokenInfo(String token, String scope) {
        HttpURLConnection connection = null;
        try {
            connection = createConnection("/oauth/token-info");
            setClientIdSecretBasicAuthHeader(connection);
            String data = new URLBuilder(connection.getURL()).addParameter("token", token)
                    .buildURL().getQuery();

            if (StringUtils.isNotEmpty(scope)) {
                data = new URLBuilder(connection.getURL()).addParameter("token", token)
                        .addParameter("scope", scope)
                        .buildURL().getQuery();
            }
            postData(connection, data);
            Map<String, Object> jsonData = getData(connection);
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
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static Map<String, Object> isSsoDeployed() {
        HttpURLConnection connection = null;
        try {
            connection = createConnection("/status");
            return getData(connection);
        } catch (FileNotFoundException ex) {
            return buildMapWithError("server_error", "oVirt Engine is initializing.");
        } catch (Exception ex) {
            return buildMapWithError("server_error", ex.getMessage());
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
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
        HttpURLConnection connection = null;
        try {
            connection = createConnection("/oauth/token-info");
            setClientIdSecretBasicAuthHeader(connection);
            URLBuilder urlBuilder = new URLBuilder(connection.getURL())
                    .addParameter("query_type", queryType)
                    .addParameter("scope", scope);
            if (StringUtils.isNotEmpty(token)) {
                urlBuilder.addParameter("token", token);
            }
            if (params != null) {
                urlBuilder.addParameter("params", encode(new JsonObjectSerializer().serialize(params)));
            }
            postData(connection, urlBuilder.buildURL().getQuery());
            return getData(connection);
        } catch (Exception ex) {
            return buildMapWithError("server_error", ex.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
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

    private static void setClientIdSecretBasicAuthHeader(HttpURLConnection connection) {
        byte[] encodedBytes = Base64.encodeBase64(String.format("%s:%s",
                EngineLocalConfig.getInstance().getProperty("ENGINE_SSO_CLIENT_ID"),
                EngineLocalConfig.getInstance().getProperty("ENGINE_SSO_CLIENT_SECRET")).getBytes());
        connection.setRequestProperty(FiltersHelper.Constants.HEADER_AUTHORIZATION, String.format("Basic %s", new String(encodedBytes)));
    }

    private static Map<String, Object> buildMapWithError(String error_code, String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", error);
        response.put("error_code", error_code);
        return response;
    }

    private static HttpURLConnection createConnection(String uri) throws Exception {
        HttpURLConnection connection = create(new URLBuilder(
                EngineLocalConfig.getInstance().getProperty("ENGINE_SSO_SERVICE_URL"),
                uri).buildURL());
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Language", "en-US");
        return connection;
    }

    private static Map getData(HttpURLConnection connection) throws Exception {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            try (InputStream input = connection.getInputStream()) {
                FiltersHelper.copy(input, os);
            }
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(SsoOAuthServiceUtils.class.getClassLoader());
            try {
                return new JsonObjectDeserializer().deserialize(
                        new String(os.toByteArray(), StandardCharsets.UTF_8.name()),
                        HashMap.class);
            } finally {
                Thread.currentThread().setContextClassLoader(loader);
            }
        }
    }

    private static void postData(HttpURLConnection connection, String data) throws Exception {
        connection.setRequestProperty("Content-Length", "" + data.length());
        connection.connect();
        try (OutputStreamWriter outputWriter = new OutputStreamWriter(connection.getOutputStream())) {
            outputWriter.write(data);
            outputWriter.flush();
        }
    }

    private static HttpURLConnection create(URL url) throws IOException, GeneralSecurityException {
        return new HttpURLConnectionBuilder(url).setHttpsProtocol(EngineLocalConfig.getInstance().getProperty("ENGINE_SSO_SERVICE_SSL_PROTOCOL"))
                .setReadTimeout(0)
                .setTrustManagerAlgorithm(TrustManagerFactory.getDefaultAlgorithm())
                .setTrustStore(EngineLocalConfig.getInstance().getProperty("ENGINE_HTTPS_PKI_TRUST_STORE"))
                .setTrustStorePassword(EngineLocalConfig.getInstance().getProperty("ENGINE_HTTPS_PKI_TRUST_STORE_PASSWORD"))
                .setTrustStoreType(EngineLocalConfig.getInstance().getProperty("ENGINE_HTTPS_PKI_TRUST_STORE_TYPE"))
                .setURL(url)
                .setVerifyChain(EngineLocalConfig.getInstance().getBoolean("ENGINE_SSO_SERVICE_SSL_VERIFY_CHAIN"))
                .setVerifyHost(EngineLocalConfig.getInstance().getBoolean("ENGINE_SSO_SERVICE_SSL_VERIFY_HOST")).create();
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
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
