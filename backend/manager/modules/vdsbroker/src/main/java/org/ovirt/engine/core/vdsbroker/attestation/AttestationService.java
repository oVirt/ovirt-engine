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
import org.apache.http.HttpStatus;
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
import org.ovirt.engine.core.common.businessentities.AttestationResultEnum;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.ssl.AuthSSLContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public enum AttestationService {
    INSTANCE;

    private static final String HEADER_HOSTS = "hosts";
    private static final String HEADER_HOST_NAME = "host_name";
    private static final String HEADER_RESULT = "trust_lvl";
    private static final String CONTENT_TYPE = "application/json";

    private static final Logger log = LoggerFactory.getLogger(AttestationService.class);

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
            if (statusCode == HttpStatus.SC_OK) {
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

    private HttpClient getClient() {
        HttpClientBuilder httpClient = HttpClients.custom();
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();

        if (Config.getValue(ConfigValues.SecureConnectionWithOATServers)) {
            try {
                TrustManagerFactory tmfactory = createTrustManagerFactory();
                AuthSSLContextFactory authSSLContextFactory =
                        new AuthSSLContextFactory(() -> Config.getValue(ConfigValues.ExternalCommunicationProtocol));
                authSSLContextFactory.setTrustManagersSupplier(tmfactory::getTrustManagers);
                authSSLContextFactory.setKeyManagersSupplier(() -> null);
                authSSLContextFactory.createSSLContext()
                        .orError(this::logSSLInitError)
                        .ifPresent(sslContext -> registryBuilder.register("https",
                                new SSLConnectionSocketFactory(sslContext)));
                httpClient.setRoutePlanner(createAttestationServerRoute());
            } catch (Exception e) {
                logSSLInitError(e.getMessage());
                log.debug("Exception", e);
            }
        }
        BasicHttpClientConnectionManager connManager = new BasicHttpClientConnectionManager(registryBuilder.build());
        httpClient.setConnectionManager(connManager);
        return httpClient.build();
    }

    private DefaultRoutePlanner createAttestationServerRoute() {
        int port = Config.getValue(ConfigValues.AttestationPort);
        String attestationServer = Config.getValue(ConfigValues.AttestationServer);
        return new DefaultRoutePlanner(host -> {
            String hostName = host.getAddress().getHostName();
            // todo is that really needed?
            if (hostName != null && hostName.contains(attestationServer)) {
                return port;
            }
            throw new UnsupportedSchemeException(" protocol is not supported for host: " + hostName);
        });
    }

    private void logSSLInitError(String error) {
        log.error("Failed to create SSL context. SSL connections will not work: {}", error);
    }

    private TrustManagerFactory createTrustManagerFactory()
            throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {
        URL trustStoreUrl = new URL("file://"
                + Config.resolveAttestationTrustStorePath());
        String truststorePassword = Config.getValue(ConfigValues.AttestationTruststorePass);

        TrustManagerFactory tmfactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmfactory.init(getTrustStore(trustStoreUrl.getPath(), truststorePassword));
        return tmfactory;
    }

    private KeyStore getTrustStore(String filePath, String password) throws IOException,
            KeyStoreException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks;
        try (InputStream in = new FileInputStream(filePath)) {
            ks = KeyStore.getInstance("JKS");
            ks.load(in, password.toCharArray());
        }
        return ks;
    }

    private List<AttestationValue> parsePostedResp(String str) throws IOException {
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

    private String writeListJson(List<String> hosts) {
        StringBuilder sb = new StringBuilder("{\"").append(HEADER_HOSTS)
                .append("\":[");
        for (String host : hosts) {
            sb.append("\"").append(host).append("\",");
        }
        return sb.substring(0, sb.length() - 1) + "]}";
    }
}
