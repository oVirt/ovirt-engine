package org.ovirt.engine.core.vdsbroker;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.utils.ssl.AuthSSLProtocolSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {

    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);
    private static final Set<String> SUPPORTED_METHODS_FOR_LONG_CONVERSION = new HashSet<>(Arrays.asList("create", "hotplugDisk"));
    static {
        if (Config.getValue(ConfigValues.EncryptHostCommunication)) {
            try {
                // registering the https protocol with a socket factory that
                // provides client authentication.
                ProtocolSocketFactory factory = new AuthSSLProtocolSocketFactory(EngineEncryptionUtils.getKeyManagers(),
                    EngineEncryptionUtils.getTrustManagers(), Config.getValue(ConfigValues.VdsmSSLProtocol));
                Protocol clientAuthHTTPS = new Protocol("https", factory, 54321);
                Protocol.registerProtocol("https", clientAuthHTTPS);
            } catch (Exception e) {
                log.error("Failed to init AuthSSLProtocolSocketFactory. SSL connections will not work", e);
            }
        }
    }

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
     * @return {@link HttpClient}.
     */
    public static HttpClient getConnection(
            int connectionTimeOut,
            int clientRetries,
            int maxConnectionsPerHost,
            int maxTotalConnections) {
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setConnectionTimeout(connectionTimeOut);
        params.setDefaultMaxConnectionsPerHost(maxConnectionsPerHost);
        params.setMaxTotalConnections(maxTotalConnections);
        MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
        httpConnectionManager.setParams(params);
        // Create the client:
        HttpClient client = new HttpClient(httpConnectionManager);

        // Configure the HTTP client so it will retry the execution of
        // methods when there are IO errors:
        int retries = Config.getValue(ConfigValues.vdsRetries);
        HttpMethodRetryHandler handler = new DefaultHttpMethodRetryHandler(retries, false);
        HttpClientParams parameters = client.getParams();
        parameters.setParameter(HttpMethodParams.RETRY_HANDLER, handler);

        // Done:
        return client;
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

    public static void shutDownConnection(HttpClient httpClient) {
        if (httpClient != null && httpClient.getHttpConnectionManager() != null) {
            ((MultiThreadedHttpConnectionManager) httpClient.getHttpConnectionManager()).shutdown();
        }
    }
}
