package org.ovirt.engine.core.vdsbroker.kubevirt;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import io.kubernetes.client.ApiException;
import openshift.io.OpenshiftApi;
import openshift.io.V1Route;
import openshift.io.V1RouteList;

@ApplicationScoped
public class PrometheusUrlResolver {
    private static final Logger log = LoggerFactory.getLogger(PrometheusUrlResolver.class);
    public static final String NOT_LOGGED_UNTIL_FAILED_AGAIN_MSG = "This error will not be logged again " +
            "until connectivity is broken again after being successfully restored";

    private final ConcurrentMap<String, LocalDateTime> openshiftConnectFailures = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LocalDateTime> prometheusUrlRetrieveFailures =
            new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LocalDateTime> prometheusUrlByRouteRetrieveFailures =
            new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LocalDateTime> prometheusUrlRouteNotFoundFailures =
            new ConcurrentHashMap<>();

    private ThrowingFunction<Provider, OpenshiftApi, IOException> openshiftApiSupplier = KubevirtUtils::getOpenshiftApi;

    @Inject
    private AuditLogDirector auditLogDirector;

    public String fetchPrometheusUrl(Provider<KubevirtProviderProperties> provider) {
        String promUrl = provider.getAdditionalProperties().getPrometheusUrl();
        if (StringUtils.isNotBlank(promUrl)) {
            return promUrl;
        }

        OpenshiftApi api;
        Optional<V1Route> route = Optional.empty();
        try {
            api = openshiftApiSupplier.apply(provider);
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
                prometheusUrlRouteNotFoundFailures.remove(provider.getUrl());
                if (prometheusUrlByRouteRetrieveFailures.remove(provider.getUrl()) != null) {
                    log.info("Prometheus url successfully retrieved for kubevirt provider (host = {}, url = {})",
                            host,
                            provider.getUrl());
                }
                return url;
            } catch (URISyntaxException e) {
                handleFailureFetchingPrometheusUrlForHost(provider, host, e);
            }
        }
        handleFailureFetchingPrometheusUrlWhenNoRouteFound(provider);
        return null;
    }

    private void handleFailureFetchingPrometheusUrlWhenNoRouteFound(Provider<KubevirtProviderProperties> provider) {
        handleFailure(provider::getUrl,
                prometheusUrlRouteNotFoundFailures,
                /* ignored */null,
                "No prometheus URL provided. Statistics won't be fetched for provider '{}' ",
                new Object[] { provider.getName() });
    }

    private void handleFailureConnectingOpenshift(Provider<KubevirtProviderProperties> provider,
            IOException cause) {
        handleFailure(provider::getUrl,
                openshiftConnectFailures,
                cause,
                "Failed to connect to openshift for kubevirt provider (url = {}): {}. ",
                new Object[] { provider.getUrl(), ExceptionUtils.getRootCauseMessage(cause) });
    }

    private void handleFailureFetchingPrometheusUrl(Provider<KubevirtProviderProperties> provider,
            ApiException cause) {
        handleFailure(provider::getUrl,
                prometheusUrlRetrieveFailures,
                cause,
                "Failed to retrieve prometheus url for kubevirt provider (url = {}): {}. ",
                new Object[] { provider.getUrl(), ExceptionUtils.getRootCauseMessage(cause) });
    }

    private void handleFailureFetchingPrometheusUrlForHost(Provider<KubevirtProviderProperties> provider,
            String host,
            URISyntaxException cause) {
        handleFailure(provider::getUrl,
                prometheusUrlByRouteRetrieveFailures,
                cause,
                "Failed to retrieve prometheus url for kubevirt provider (host = {}, url = {}): {}. ",
                new Object[] { host, provider.getUrl(), ExceptionUtils.getRootCauseMessage(cause) });
    }

    private void handleFailure(Supplier<String> idSupplier,
            ConcurrentMap<String, LocalDateTime> failures,
            Throwable cause,
            String errMsg,
            Object[] errArgs) {

        LocalDateTime firstFailure = failures.putIfAbsent(idSupplier.get(), LocalDateTime.now());
        if (firstFailure == null) {
            if (log.isErrorEnabled()) {
                log.error(errMsg + NOT_LOGGED_UNTIL_FAILED_AGAIN_MSG, errArgs);
                if (cause != null) {
                    log.debug("Exception", cause);
                }
            }
        } else if (log.isDebugEnabled()) {
            Object[] continuationOfFailureMsgArgs = Arrays.copyOf(errArgs, errArgs.length + 1);
            continuationOfFailureMsgArgs[errArgs.length] = firstFailure;
            log.debug("Continuation of: " + errMsg + " First failure: {}", continuationOfFailureMsgArgs);
        }
    }

    @VisibleForTesting
    void setOpenshiftApiSupplier(ThrowingFunction<Provider, OpenshiftApi, IOException> openshiftApiSupplier) {
        this.openshiftApiSupplier = openshiftApiSupplier;
    }

    @VisibleForTesting
    void setAuditLogDirector(AuditLogDirector auditLogDirector) {
        this.auditLogDirector = auditLogDirector;
    }

    @FunctionalInterface
    interface ThrowingFunction<T, R, E extends Exception> {
        R apply(T t) throws E;
    }
}
