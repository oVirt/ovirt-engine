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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.core.common.businessentities.AttestationResultEnum;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.ssl.AuthSSLProtocolSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttestationService {
    private static final String HEADER_HOSTS = "hosts";
    private static final String HEADER_HOST_NAME = "host_name";
    private static final String HEADER_RESULT = "trust_lvl";
    private static final String HEADER_VTIME = "vtime";
    private static final String CONTENT_TYPE = "application/json";
    private static final AttestationService instance = new AttestationService();

    private static final Logger log = LoggerFactory.getLogger(AttestationService.class);

    public static HttpClient getClient() {
        HttpClient httpClient = new HttpClient();
        if (Config
                .<Boolean> getValue(ConfigValues.SecureConnectionWithOATServers)) {
            URL trustStoreUrl;
            try {
                int port = Config
                        .<Integer> getValue(ConfigValues.AttestationPort);
                trustStoreUrl = new URL("file://"
                        + Config.resolveAttestationTrustStorePath());
                String truststorePassword = Config
                        .<String> getValue(ConfigValues.AttestationTruststorePass);
                String attestationServer = Config
                        .<String> getValue(ConfigValues.AttestationServer);
                // registering the https protocol with a socket factory that
                // provides client authentication.
                ProtocolSocketFactory factory = new AuthSSLProtocolSocketFactory(getTrustStore(trustStoreUrl.getPath(),
                        truststorePassword), Config.<String> getValue(ConfigValues.ExternalCommunicationProtocol));
                Protocol clientAuthHTTPS = new Protocol("https", factory, port);
                httpClient.getHostConfiguration().setHost(attestationServer,
                        port, clientAuthHTTPS);
            } catch (Exception e) {
                log.error("Failed to init AuthSSLProtocolSocketFactory. SSL connections will not work: {}", e.getMessage());
                log.debug("Exception", e);
            }
        }
        return httpClient;
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

    private AttestationService() {
    }

    public List<AttestationValue> attestHosts(List<String> hosts) {
        String pollURI = Config.<String> getValue(ConfigValues.PollUri);
        List<AttestationValue> values = new ArrayList<>();

        PostMethod postMethod = new PostMethod("/" + pollURI);
        try {
            postMethod.setRequestEntity(new StringRequestEntity(
                    writeListJson(hosts)));
            postMethod.addRequestHeader("Accept", CONTENT_TYPE);
            postMethod.addRequestHeader("Content-type", CONTENT_TYPE);
            HttpClient httpClient = getClient();
            int statusCode = httpClient.executeMethod(postMethod);
            String strResponse = postMethod.getResponseBodyAsString();
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
