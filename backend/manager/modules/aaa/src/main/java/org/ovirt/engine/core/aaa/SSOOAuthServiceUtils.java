package org.ovirt.engine.core.aaa;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.TrustManagerFactory;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.aaa.filters.FiltersHelper;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;
import org.ovirt.engine.core.uutils.net.HttpURLConnectionBuilder;
import org.ovirt.engine.core.uutils.net.URLBuilder;

public class SSOOAuthServiceUtils {

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

    public static Map<String, Object> loginOnBehalf(String username, String scope) {
        return loginWithPassword(username, "", scope);
    }

    public static Map<String, Object> loginWithPassword(String username, String password, String scope) {
        HttpURLConnection connection = null;
        try {
            connection = createConnection("/oauth/token");
            setClientIdSecretBasicAuthHeader(connection);
            postData(connection, new URLBuilder(connection.getURL()).addParameter("grant_type", "password")
                    .addParameter("username", username)
                    .addParameter("password", password)
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
            return getData(connection);
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
            Thread.currentThread().setContextClassLoader(SSOOAuthServiceUtils.class.getClassLoader());
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
                .setTrustStore(EngineLocalConfig.getInstance().getProperty("ENGINE_PKI_TRUST_STORE"))
                .setTrustStorePassword(EngineLocalConfig.getInstance().getPKITrustStorePassword())
                .setTrustStoreType(EngineLocalConfig.getInstance().getPKITrustStoreType())
                .setURL(url)
                .setVerifyChain(EngineLocalConfig.getInstance().getBoolean("ENGINE_SSO_SERVICE_SSL_VERIFY_CHAIN"))
                .setVerifyHost(EngineLocalConfig.getInstance().getBoolean("ENGINE_SSO_SERVICE_SSL_VERIFY_HOST")).create();
    }

}
