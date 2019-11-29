package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.vdsbroker.KubevirtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.ApiException;
import openshift.io.OpenshiftApi;
import openshift.io.V1Route;
import openshift.io.V1RouteList;


public class PrometheusClient {

    public static final long BYTES_IN_KiB = 1024;
    public static final long BYTES_IN_MiB = BYTES_IN_KiB * BYTES_IN_KiB;

    private static final NoCaTrustManager noCaTrustManager = new NoCaTrustManager();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static Logger log = LoggerFactory.getLogger(PrometheusClient.class);

    private HttpClient httpClient;
    private Provider<KubevirtProviderProperties> provider;
    private String promUrl;

    public PrometheusClient(Provider<KubevirtProviderProperties> provider, String promUrl) {
        this(provider, promUrl, null);
    }

    public PrometheusClient(Provider<KubevirtProviderProperties> provider, String promUrl, SSLContext sslContext) {
        this.provider = provider;
        this.promUrl = promUrl;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .sslContext(sslContext)
                .build();
    }

    public static PrometheusClient create(Provider<KubevirtProviderProperties> provider) {
        String promUrl = provider.getAdditionalProperties().getPrometheusUrl();
        if (promUrl == null) {
            promUrl = fetchPrometheusUrl(provider);
            if (promUrl == null) {
                log.error(
                    "No prometheus URL provided. Statistics won't be fetched for provider '{}'", provider.getName()
                );
                return null;
            }
        }
        if (promUrl.startsWith("https")) {
            return new PrometheusClient(provider, promUrl, getContext(provider));
        } else {
            return new PrometheusClient(provider, promUrl);
        }
    }

    private static SSLContext getContext(Provider<KubevirtProviderProperties> provider) {
        try {
            String ca = provider.getAdditionalProperties().getPrometheusCertificateAuthority();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            if (ca == null) {
                sslContext.init(null, new TrustManager[]{noCaTrustManager}, null);
            } else {
                byte[] promCA = Base64.getDecoder().decode(ca);
                InputStream is = new ByteArrayInputStream(promCA);
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate caCert = (X509Certificate) cf.generateCertificate(is);

                TrustManagerFactory tmf = TrustManagerFactory
                        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(null);
                ks.setCertificateEntry("caCert", caCert);

                tmf.init(ks);
                sslContext.init(null, tmf.getTrustManagers(), null);
            }
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String fetchPrometheusUrl(Provider<KubevirtProviderProperties> provider) {
        OpenshiftApi api;
        Optional<V1Route> route = null;
        try {
            api = KubevirtUtils.getOpenshiftApi(provider);
        } catch (IOException e) {
            log.error("failed to connect to openshift for kubevirt provider (url = {}): {}",
                    provider.getUrl(),
                    ExceptionUtils.getRootCauseMessage(e));
            log.debug("Exception", e);
            return null;
        }

        try {
            V1RouteList routes = api.listNamespacedRoute("openshift-monitoring",
                    null,
                    "metadata.name=prometheus-k8s",
                    null,
                    null,
                    null,
                    null,
                    null,
                    Boolean.FALSE);
            route = routes.getItems().stream().findAny();
        } catch (ApiException e) {
            log.error("failed to retrieve prometheus url for kubevirt provider (url = {}): {}",
                    provider.getUrl(),
                    ExceptionUtils.getRootCauseMessage(e));
            log.debug("Exception", e);
        }

        if (route != null && route.isPresent()) {
            String host = route.get().getSpec().getHost();
            try {
                return new URI("https", host, null, null).toString();
            } catch (URISyntaxException e) {
                log.error("Failed to retrieve prometheus url for kubevirt provider (host = {}, url = {}): {}",
                        host,
                        provider.getUrl(),
                        ExceptionUtils.getRootCauseMessage(e));
                log.debug("Exception", e);
            }
        }

        return null;
    }

    private Long bToMb(Long bytes) {
        return bytes/BYTES_IN_MiB;
    }

    private JsonNode getMetric(String query, String instance, String params) {
        return getMetric(String.format("%1$s{instance='%2$s', %3$s}", query, instance, params));
    }

    private JsonNode getMetric(String query, String instance) {
        return getMetric(String.format("%1$s{instance='%2$s'}", query, instance));
    }

    private JsonNode getMetric(String query) {
        HttpResponse<String> response = request(query);
        // TODO: check for errors.
        try {
            JsonNode node = mapper.readTree(response.body().getBytes());
            return node.get("data").get("result").get(0).get("value").get(1);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse<String> request(String query) {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(ofFormData(Map.of("query", query)))
                .uri(URI.create(String.format("%1$s/api/v1/query?", this.promUrl)))
                .header("User-Agent", "oVirt Engine")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        return send(request);
    }

    private static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException|InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static class NoCaTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                throws java.security.cert.CertificateException {
            // do nothing
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                throws java.security.cert.CertificateException {
            // do nothing

        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
