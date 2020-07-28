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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.DoubleNode;
import org.codehaus.jackson.node.IntNode;
import org.codehaus.jackson.node.NumericNode;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.ApiException;
import openshift.io.OpenshiftApi;
import openshift.io.V1Route;
import openshift.io.V1RouteList;

public class PrometheusClient implements Closeable {

    public static final long BYTES_IN_KiB = 1024;
    public static final long BYTES_IN_MiB = BYTES_IN_KiB * BYTES_IN_KiB;

    private static final Logger log = LoggerFactory.getLogger(PrometheusClient.class);
    private static final NoCaTrustManager noCaTrustManager = new NoCaTrustManager();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ConcurrentMap<String, LocalDateTime> openshiftConnectFailures = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, LocalDateTime> prometheusUrlRetrieveFailures =
            new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, LocalDateTime> prometheusUrlByRouteRetrieveFailures =
            new ConcurrentHashMap<>();
    private static final ConcurrentMap<PrometheusQueryId, LocalDateTime> metricsQueriesFailures =
            new ConcurrentHashMap<>();

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
                        nodeName).asDouble() * 100);
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
                bToKb(getMetric("node_memory_Hugepagesize_bytes{instance='%1$s'}", nodeName).asLong()).intValue()));
    }

    // boot
    public Long getNodeBootTime(String nodeName) {
        return getMetric("node_boot_time_seconds{instance='%1$s'}", nodeName).asLong();
    }

    // CPU
    public Double getNodeCpuIdle(String nodeName) {
        return getMetric(
                "avg by (mode) (irate(node_cpu_seconds_total{instance='%1$s', mode='idle'}[1m])) * 100",
                nodeName).asDouble();
    }

    public Double getNodeCpuSystem(String nodeName) {
        return getMetric(
                "avg by (mode) (irate(node_cpu_seconds_total{instance='%1$s', mode='system'}[1m])) * 100",
                nodeName).asDouble();
    }

    public Double getNodeCpuUser(String nodeName) {
        return getMetric(
                "avg by (mode) (irate(node_cpu_seconds_total{instance='%1$s', mode='user'}[1m])) * 100",
                nodeName).asDouble();
    }

    public Double getNodeCpuLoad(String nodeName) {
        return getMetric(
                "100 - (avg without (mode) (rate(node_cpu_seconds_total{instance='%1$s', mode='idle'}[5m])) * 100)",
                nodeName).asDouble();
    }

    public Integer getNodeCpuUsage(String nodeName) {
        return (int) Math.round(getMetric(
                "100 - (avg without (cpu, mode) (rate(node_cpu_seconds_total{instance='%1$s', mode='idle'}[1m])) * 100)",
                nodeName).asDouble());
    }

    // KSM

    //
    // VM
    //

    // CPU usage
    public Double getVmiCpuUsage(String vmiName, String vmiNamespace) {
        return getMetric(
                "rate(kubevirt_vmi_vcpu_seconds{name='%1$s', exported_namespace='%2$s'}[1m]) * 100",
                vmiName,
                vmiNamespace).asDouble();
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
                sslContext.init(null, new TrustManager[] { noCaTrustManager }, null);
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

    public static String fetchPrometheusUrl(Provider<KubevirtProviderProperties> provider,
            AuditLogDirector auditLogDirector) {
        OpenshiftApi api;
        Optional<V1Route> route = Optional.empty();
        try {
            api = KubevirtUtils.getOpenshiftApi(provider);
            if (openshiftConnectFailures.remove(provider.getUrl()) != null) {
                log.info("Successfully re-connected to openshift for kubevirt provider (url = {})", provider.getUrl());
            }
        } catch (IOException e) {
            handleFailureConnectingOpenshift(provider, e);
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
            if (prometheusUrlRetrieveFailures.remove(provider.getUrl()) != null) {
                log.info("Prometheus url successfully retrieved for kubevirt provider (url = {})", provider.getUrl());
            }
        } catch (ApiException e) {
            KubevirtAuditUtils.auditAuthorizationIssues(e, auditLogDirector, provider);
            handleFailureFetchingPrometheusUrl(provider, e);
        }

        if (route.isPresent()) {
            String host = route.get().getSpec().getHost();
            try {
                // TODO: Test if it's up and running before returning:
                log.debug("Found Prometheus route '{}'", host);
                String url = new URI("https", host, null, null).toString();
                if (prometheusUrlByRouteRetrieveFailures.remove(provider.getUrl()) != null) {
                    log.info("Prometheus url successfully retrieved for kubevirt provider (host = {}, url = {})",
                            host,
                            provider.getUrl());
                }
                return url;
            } catch (URISyntaxException e) {
                handleFailureFetchingPrometheusUrlForHost(provider, host, e);
            }
        } else {
            log.error("No prometheus URL provided. Statistics won't be fetched for provider '{}'", provider.getName());
        }
        return null;
    }

    private static void handleFailureConnectingOpenshift(Provider<KubevirtProviderProperties> provider,
            IOException cause) {
        handleFailure(provider::getUrl,
                openshiftConnectFailures,
                cause,
                ignored -> log.error("Failed to connect to openshift for kubevirt provider (url = {}): {}. " +
                                "This error will not be logged again " +
                                "until connectivity is broken again after being successfully restored",
                        provider.getUrl(),
                        ExceptionUtils.getRootCauseMessage(cause)),
                firstFailure -> log.debug(
                        "Still failed to connect to openshift for kubevirt provider (url= {}): {}), first failure: {}",
                        provider.getUrl(),
                        cause,
                        firstFailure));
    }

    private static void handleFailureFetchingPrometheusUrl(Provider<KubevirtProviderProperties> provider,
            ApiException cause) {
        handleFailure(provider::getUrl,
                prometheusUrlRetrieveFailures,
                cause,
                ignored -> log.error("Failed to retrieve prometheus url for kubevirt provider (url = {}): {}. " +
                                "This error will not be logged again " +
                                "until connectivity is broken again after being successfully restored",
                        provider.getUrl(),
                        ExceptionUtils.getRootCauseMessage(cause)),
                firstFailure -> log.debug(
                        "Still failed to retrieve prometheus url for kubevirt provider (url= {}): {}), first failure: {}",
                        provider.getUrl(),
                        cause,
                        firstFailure));
    }

    private static void handleFailureFetchingPrometheusUrlForHost(Provider<KubevirtProviderProperties> provider,
            String host,
            URISyntaxException cause) {
        handleFailure(() -> provider.getUrl() + "|" + host,
                prometheusUrlByRouteRetrieveFailures,
                cause,
                ignored -> log.error(
                        "Failed to retrieve prometheus url for kubevirt provider (host = {}, url = {}): {}. " +
                                "This error will not be logged again " +
                                "until connectivity is broken again after being successfully restored",
                        host,
                        provider.getUrl(),
                        ExceptionUtils.getRootCauseMessage(cause)),
                firstFailure -> log.debug(
                        "Still failed to retrieve prometheus url for kubevirt provider (host = {}, url= {}): {}), first failure: {}",
                        host,
                        provider.getUrl(),
                        cause,
                        firstFailure));
    }

    private static void handleFailure(Supplier<String> idSupplier,
            ConcurrentMap<String, LocalDateTime> failures,
            Throwable cause,
            Consumer<Void> errorLoggingStatement,
            Consumer<LocalDateTime> continuingErrorLoggingStatement) {

        LocalDateTime firstFailure = failures.putIfAbsent(idSupplier.get(), LocalDateTime.now());
        if (firstFailure == null) {
            if (log.isErrorEnabled()) {
                errorLoggingStatement.accept(null);
                log.debug("Exception", cause);
            }
        } else {
            continuingErrorLoggingStatement.accept(firstFailure);
        }
    }

    private Long bToMb(Long bytes) {
        return bytes / BYTES_IN_MiB;
    }

    private Long bToKb(Long bytes) {
        return bytes / BYTES_IN_KiB;
    }

    private NumericNode getMetric(String query, String... params) {
        NumericNode retVal = new IntNode(0);
        query = String.format(query, params);
        if (promUrl == null) {
            return retVal;
        }

        try (CloseableHttpResponse response = request(query)) {
            if (response == null) {
                return retVal;
            }
            if (handleMetricsFetchFailure(new PrometheusQueryId(promUrl, query), response)) {
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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return retVal;
    }

    private boolean handleMetricsFetchFailure(PrometheusQueryId queryId, CloseableHttpResponse response) {
        if (response.getStatusLine().getStatusCode() >= 300) {
            LocalDateTime failureTime = LocalDateTime.now();
            LocalDateTime firstFailureTime = metricsQueriesFailures.putIfAbsent(queryId, failureTime);
            if (firstFailureTime == null) {
                if (log.isWarnEnabled()) {
                    log.warn(
                            "Failed to fetch metrics from {} for query: {}. " +
                                    "This error will not be logged again " +
                                    "until connectivity is broken again after being successfully restored",
                            queryId.prometheusUrl,
                            queryId.query);
                    try {
                        String body = EntityUtils.toString(response.getEntity());
                        log.debug("Failed to fetch metrics from {}. Details: {} {}",
                                queryId.prometheusUrl,
                                queryId.query,
                                body);
                    } catch (ParseException | IOException e) {
                        log.warn("Failed to parse failed fetch metric from {} for {}",
                                queryId.prometheusUrl,
                                queryId.query);
                    }
                }
            } else {
                log.debug("Still failed to fetched metrics from {} for query: {}, since: {}",
                        queryId.prometheusUrl,
                        queryId.query,
                        firstFailureTime);
            }
            return true;
        } else if (metricsQueriesFailures.remove(queryId) != null) {
            // metrics fetching restored, let it be announced
            log.info("Metrics successfully retrieved from {} after failure for query: {}",
                    queryId.prometheusUrl,
                    queryId.query);
        }
        return false;
    }

    private CloseableHttpResponse request(String query) {
        HttpPost request =
                new HttpPost(String.format("%1$s/api/v1/query?%2$s", this.promUrl, ofFormData(Map.of("query", query))));
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
            log.error("Failed to contact the Prometheus server: {} cause: {}", promUrl, ExceptionUtils.getRootCause(e));
            log.debug("Exception: ", e);
            return null;
        }
    }

    private static class NoCaTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            // do nothing
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
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

    private static class PrometheusQueryId {
        private final String prometheusUrl;
        private final String query;

        private PrometheusQueryId(String prometheusUrl, String query) {
            this.prometheusUrl = prometheusUrl;
            this.query = query;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PrometheusQueryId that = (PrometheusQueryId) o;
            return Objects.equals(prometheusUrl, that.prometheusUrl) &&
                    Objects.equals(query, that.query);
        }

        @Override
        public int hashCode() {
            return Objects.hash(prometheusUrl, query);
        }
    }
}
