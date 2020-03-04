package org.ovirt.engine.core.vdsbroker.kubevirt;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.DoubleNode;
import org.codehaus.jackson.node.IntNode;
import org.codehaus.jackson.node.NumericNode;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.ApiException;
import openshift.io.OpenshiftApi;
import openshift.io.V1Route;
import openshift.io.V1RouteList;


public class PrometheusClient implements Closeable {

    public static final long BYTES_IN_KiB = 1024;
    public static final long BYTES_IN_MiB = BYTES_IN_KiB * BYTES_IN_KiB;

    private static final NoCaTrustManager noCaTrustManager = new NoCaTrustManager();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static Logger log = LoggerFactory.getLogger(PrometheusClient.class);

    private CloseableHttpClient httpClient;
    private Provider<KubevirtProviderProperties> provider;
    private String promUrl;

    public PrometheusClient(Provider<KubevirtProviderProperties> provider, String promUrl) {
        this.provider = provider;
        this.promUrl = promUrl;
        this.httpClient = newClient(null);
    }

    public PrometheusClient(Provider<KubevirtProviderProperties> provider, String promUrl, SSLContext sslContext) {
        this.provider = provider;
        this.promUrl = promUrl;
        this.httpClient = newClient(sslContext);
    }

    private CloseableHttpClient newClient(SSLContext sslContext) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        if (sslContext != null) {
            builder.setSSLContext(sslContext);
        }
        return builder.build();
    }

    //
    // Host
    //

    // Swap
    public Long getNodeSwapTotalMb(String nodeName) {
        return bToMb(getMetric("node_memory_SwapTotal_bytes{instance='%1$s'}", nodeName).asLong());
    }

    public Long getNodeSwapFreeMb(String nodeName) {
        return bToMb(getMetric("node_memory_SwapFree_bytes{instance='%1$s'}", nodeName).asLong());
    }

    // Memory
    public Integer getNodeMemUsage(String nodeName) {
        return (int) Math.round(
            getMetric(
                "((node_memory_MemTotal_bytes{instance='%1$s'}-node_memory_MemFree_bytes{instance='%1$s'})/" +
                    "node_memory_MemTotal_bytes{instance='%1$s'})",
                nodeName
        ).asDouble() * 100);
    }

    public Long getNodeMemFree(String nodeName) {
        return bToMb(getMetric("node_memory_MemFree_bytes{instance='%1$s'}", nodeName).asLong());
    }

    public Long getNodeMemAvailable(String nodeName) {
        return bToMb(getMetric("node_memory_MemAvailable_bytes{instance='%1$s'}", nodeName).asLong());
    }

    public Long getNodeMemShared(String nodeName) {
        return bToMb(getMetric("node_memory_Shmem_bytes{instance='%1$s'}", nodeName).asLong());
    }

    // Huge pages
    public Integer getNodeAnonHugePages(String nodeName) {
        return getMetric("node_memory_AnonHugePages_bytes{instance='%1$s'}", nodeName).asInt();
    }

    public List<Pair<Integer, Integer>> getNodeHugePages(String nodeName) {
        return Arrays.asList(new Pair<>(
            getMetric("node_memory_HugePages_Free{instance='%1$s'}", nodeName).asInt(),
            bToKb(getMetric("node_memory_Hugepagesize_bytes{instance='%1$s'}", nodeName).asLong()).intValue()
        ));
    }

    // boot
    public Long getNodeBootTime(String nodeName) {
        return getMetric("node_boot_time_seconds{instance='%1$s'}", nodeName).asLong();
    }

    // CPU
    public Double getNodeCpuIdle(String nodeName) {
        return getMetric(
            "avg by (mode) (irate(node_cpu_seconds_total{instance='%1$s', mode='idle'}[1m])) * 100", nodeName
        ).asDouble();
    }

    public Double getNodeCpuSystem(String nodeName) {
        return getMetric(
                "avg by (mode) (irate(node_cpu_seconds_total{instance='%1$s', mode='system'}[1m])) * 100", nodeName
        ).asDouble();
    }

    public Double getNodeCpuUser(String nodeName) {
        return getMetric(
                "avg by (mode) (irate(node_cpu_seconds_total{instance='%1$s', mode='user'}[1m])) * 100", nodeName
        ).asDouble();
    }

    public Double getNodeCpuLoad(String nodeName) {
        return getMetric(
            "100 - (avg without (mode) (rate(node_cpu_seconds_total{instance='%1$s', mode='idle'}[5m])) * 100)", nodeName
        ).asDouble();
    }

    public Integer getNodeCpuUsage(String nodeName) {
        return (int) Math.round(getMetric(
            "100 - (avg without (cpu, mode) (rate(node_cpu_seconds_total{instance='%1$s', mode='idle'}[1m])) * 100)", nodeName
        ).asDouble());
    }

    // KSM

    //
    // VM
    //

    // CPU usage
    public Double getVmiCpuUsage(String vmiName, String vmiNamespace) {
        return getMetric(
                "rate(kubevirt_vmi_vcpu_seconds{name='%1$s', exported_namespace='%2$s'}[1m]) * 100", vmiName, vmiNamespace
        ).asDouble();
    }

    // Prometheus CA is prio 1. if it's not specified, try to use Openshift CA:
    private static String getPrometheusCa(Provider<KubevirtProviderProperties> provider) {
        String ca = provider.getAdditionalProperties().getPrometheusCertificateAuthority();
        if (ca == null) {
            ca = provider.getAdditionalProperties().getCertificateAuthority();
        }
        return ca;
    }

    public static SSLContext getContext(Provider<KubevirtProviderProperties> provider) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            String ca = getPrometheusCa(provider);
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

    public static String fetchPrometheusUrl(Provider<KubevirtProviderProperties> provider) {
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
                // TODO: Test if it's up and running before returning:
                log.debug("Found Prometheus route '{}'", host);
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

    private Long bToKb(Long bytes) {
        return bytes/BYTES_IN_KiB;
    }

    private NumericNode getMetric(String query, String ... params) {
        NumericNode retVal = new IntNode(0);
        query = String.format(query, params);
        if (promUrl == null) {
            return retVal;
        }

        try (CloseableHttpResponse response = request(query)) {
            if (response == null) {
                return retVal;
            }
            if (response.getStatusLine().getStatusCode() >= 300) {
                String body;
                try {
                    body = EntityUtils.toString(response.getEntity());
                    log.warn("Failed to fetch metric {}: {}", query, body);
                } catch (ParseException | IOException e) {
                    log.warn("Failed to parse failed fetch metric for {}", query);
                }
                return retVal;
            }
            try {
                JsonNode node = mapper.readTree(EntityUtils.toByteArray(response.getEntity()));
                if (node.has("status")) {
                    if (node.get("status").asText().equals("success")) {
                        JsonNode result = node.get("data").get("result");
                        if (result.size() > 0) {
                            JsonNode value = result.get(0).get("value");
                            return new DoubleNode(Double.parseDouble(value.get(1).asText()));
                        }
                    } else {
                        log.warn("Query '{}' failed to execute: {} - {}", query, node.get("status"), node.get("error"));
                    }
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return retVal;
    }

    private CloseableHttpResponse request(String query) {
        HttpPost request = new HttpPost(String.format("%1$s/api/v1/query?%2$s", this.promUrl, ofFormData(Map.of("query", query))));
        request.addHeader("User-Agent", "oVirt Engine");
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        request.addHeader("Authorization", String.format("Bearer %s", provider.getPassword()));

        return send(request);
    }

    private static String ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    private CloseableHttpResponse send(HttpUriRequest request) {
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            log.error("Failed to contact the Prometheus server: {}", promUrl, e.getMessage());
            log.debug("Exception: ", e);
            return null;
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

    @Override
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}
