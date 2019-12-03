package org.ovirt.engine.core.vdsbroker;

import static org.apache.http.protocol.HTTP.CONTENT_LEN;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.utils.ssl.AuthSSLContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {

    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    // todo will be injectable in futher patches (HttpUtils will no longer be 'static' based)
    private static final AuthSSLContextFactory authSslContextFactory =
            new AuthSSLContextFactory(() -> Config.getValue(ConfigValues.VdsmSSLProtocol));

    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * There are places in the code where use http to communicate with vdsm or external providers
     *
     * @param connectionTimeOut
     *            - the instance type of the interface for this connection
     * @param clientRetries
     *            - Number of retries if timeout occurd
     * @param maxConnectionsPerHost
     *            - maximum number of connections allowed for a given host
     * @param maxTotalConnections
     *            - The maximum number of connections allowed
     * @return {@link CloseableHttpClient}.
     */
    public static CloseableHttpClient getConnection(
            int connectionTimeOut,
            int clientRetries,
            int maxConnectionsPerHost,
            int maxTotalConnections) {

        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory> create()
                // todo: should it be registered by default - need to be verified
                .register(HTTP, PlainConnectionSocketFactory.INSTANCE);

        if (Config.getValue(ConfigValues.EncryptHostCommunication)) {
            authSslContextFactory.createSSLContext()
                    .orError(err -> log.error("Failed to init SSL factory. SSL connections will not work. {}", err))
                    .ifPresent(sslContext -> registryBuilder.register(HTTPS,
                            new SSLConnectionSocketFactory(sslContext)));
        }

        DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(clientRetries, false);

        RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectTimeout(connectionTimeOut).build();

        PoolingHttpClientConnectionManager connManager =
                new PoolingHttpClientConnectionManager(registryBuilder.build());
        connManager.setDefaultConnectionConfig(ConnectionConfig.DEFAULT);
        connManager.setMaxTotal(maxTotalConnections);
        connManager.setDefaultMaxPerRoute(maxConnectionsPerHost);

        return HttpClients.custom()
                .addInterceptorFirst(new ContentLengthHeaderRemover())
                .setConnectionManager(connManager)
                .setMaxConnPerRoute(maxConnectionsPerHost)
                .setMaxConnTotal(maxTotalConnections)
                .setRetryHandler(retryHandler)
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();
    }

    public static Pair<String, URL> getConnectionUrl(String hostName, int port, String path, boolean isSecure) {
        final String protocol = isSecure ? HTTPS : HTTP;
        try {
            URL url = new URL(protocol, hostName, port, path != null ? "/" + path : "");
            return new Pair<>(url.toString(), url);
        } catch (MalformedURLException mfue) {
            log.error("failed to form the xml-rpc url", mfue);
            throw new IllegalStateException(mfue);
        }
    }

    public static void shutDownConnection(CloseableHttpClient httpClient) {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.debug("Http client shutdown error details", e);
                log.warn("Http client shutdown error: {}", e.getMessage());
            }
        }
    }

    private static class ContentLengthHeaderRemover implements HttpRequestInterceptor {
        @Override
        public void process(HttpRequest request, HttpContext context) {
            request.removeHeaders(CONTENT_LEN);
        }
    }
}
