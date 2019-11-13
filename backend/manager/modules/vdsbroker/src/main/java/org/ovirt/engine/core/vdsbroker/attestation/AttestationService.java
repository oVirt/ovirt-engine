package org.ovirt.engine.core.vdsbroker.attestation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.TrustManagerFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.UnsupportedSchemeException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.core.common.businessentities.AttestationResultEnum;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.ssl.AuthSSLContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttestationService {
    private static final String HEADER_HOSTS = "hosts";
    private static final String HEADER_HOST_NAME = "host_name";
    private static final String HEADER_RESULT = "trust_lvl";
    private static final String CONTENT_TYPE = "application/json";
    private static final AttestationService instance = new AttestationService();

    private static final Logger log = LoggerFactory.getLogger(AttestationService.class);

    private AttestationService() {
    }

    public static HttpClient getClient() {
        HttpClientBuilder httpClient = HttpClients.custom();
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();

        if (Config.getValue(ConfigValues.SecureConnectionWithOATServers)) {
            try {
                int port = Config.getValue(ConfigValues.AttestationPort);
                URL trustStoreUrl = new URL("file://"
                        + Config.resolveAttestationTrustStorePath());
                String truststorePassword = Config.getValue(ConfigValues.AttestationTruststorePass);
                String attestationServer = Config.getValue(ConfigValues.AttestationServer);

                TrustManagerFactory tmfactory =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmfactory.init(getTrustStore(trustStoreUrl.getPath(), truststorePassword));
                AuthSSLContextFactory authSSLContextFactory =
                        new AuthSSLContextFactory(() -> Config.getValue(ConfigValues.ExternalCommunicationProtocol));
                authSSLContextFactory.setTrustManagersSupplier(tmfactory::getTrustManagers);
                authSSLContextFactory.setKeyManagersSupplier(() -> null);

                authSSLContextFactory.createSSLContext()
                        .orError(e -> log.error(
                                "Failed to init AuthSSLProtocolSocketFactory. SSL connections will not work: {}",
                                e))
                        .ifPresent(sslContext -> registryBuilder.register("https",
                                new SSLConnectionSocketFactory(sslContext)));

                httpClient.setRoutePlanner(new DefaultRoutePlanner(host -> {
                    String hostName = host.getAddress().getHostName();
                    // todo is that really needed?
                    if (hostName != null && hostName.contains(attestationServer)) {
                        return port;
                    }
                    throw new UnsupportedSchemeException(" protocol is not supported for host: " + hostName);
                }));

            } catch (Exception e) {
                log.error("Failed to init AuthSSLProtocolSocketFactory. SSL connections will not work: {}",
                        e.getMessage());
                log.debug("Exception", e);
            }
        }

        BasicHttpClientConnectionManager connManager = new BasicHttpClientConnectionManager(registryBuilder.build());
        httpClient.setConnectionManager(connManager);
        return httpClient.build();
    }

    private static KeyStore getTrustStore(String filePath, String password) throws IOException,
            KeyStoreException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks;
        try (InputStream in = new FileInputStream(filePath)) {
            ks = KeyStore.getInstance("JKS");
            ks.load(in, password.toCharArray());
        }

        return ks;
    }

    public static AttestationService getInstance() {
        return instance;
    }

    public List<AttestationValue> attestHosts(List<String> hosts) {
        String pollURI = Config.getValue(ConfigValues.PollUri);
        List<AttestationValue> values = new ArrayList<>();

        HttpPost postMethod = new HttpPost("/" + pollURI);
        try {

            postMethod.setEntity(new StringEntity(writeListJson(hosts)));
            postMethod.addHeader("Accept", CONTENT_TYPE);
            postMethod.addHeader("Content-type", CONTENT_TYPE);
            HttpClient httpClient = getClient();
            HttpResponse response = httpClient.execute(postMethod);
            int statusCode = response.getStatusLine().getStatusCode();
            String strResponse = EntityUtils.toString(response.getEntity());
            log.debug("return attested result: {}", strResponse);
            if (statusCode == 200) {
                values = parsePostedResp(strResponse);
            } else {
                log.error("attestation error: {}", strResponse);
            }
        } catch (JsonParseException e) {
            log.error("Failed to parse result: {}", e.getMessage());
            log.debug("Exception", e);
        } catch (IOException e) {
            log.error("Failed to attest hosts, make sure hosts are up and reachable: {}",
                    e.getMessage());
            log.debug("Exception", e);
        } finally {
            postMethod.releaseConnection();
        }
        return values;
    }

    public List<AttestationValue> parsePostedResp(String str)
            throws JsonProcessingException, IOException {
        List<AttestationValue> values = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode tree = mapper.readTree(str);

        JsonNode hosts = tree.get(HEADER_HOSTS);
        if (hosts != null) {
            for (JsonNode host : hosts) {
                String name = host.get(HEADER_HOST_NAME).asText();
                String level = host.get(HEADER_RESULT).asText();
                AttestationValue value = new AttestationValue();
                value.setHostName(name);
                value.setTrustLevel(AttestationResultEnum.valueOf(level.toUpperCase()));
                values.add(value);
            }
        }
        return values;
    }

    public String writeListJson(List<String> hosts) {
        StringBuilder sb = new StringBuilder("{\"").append(HEADER_HOSTS)
                .append("\":[");
        for (String host : hosts) {
            sb = sb.append("\"").append(host).append("\",");
        }
        String jsonString = sb.substring(0, sb.length() - 1) + "]}";
        return jsonString;
    }
}
