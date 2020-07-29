package org.ovirt.engine.core.vdsbroker.kubevirt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

import io.kubernetes.client.ApiException;
import openshift.io.OpenshiftApi;
import openshift.io.V1Route;
import openshift.io.V1RouteList;
import openshift.io.V1RouteSpec;

@ExtendWith(MockitoExtension.class)
class PrometheusUrlResolverTest {
    public static final String TEST_HOST = "prometheus.test.example.org";
    private static final String HTTPS_URL = "https://" + TEST_HOST;

    @Mock
    private Provider<KubevirtProviderProperties> provider;

    @Mock
    private AuditLogDirector auditLogDirector;

    @Mock
    private OpenshiftApi openshiftApi;

    private PrometheusUrlResolver urlResolver;

    @BeforeEach
    public void setup() {
        urlResolver = new PrometheusUrlResolver();
        urlResolver.setAuditLogDirector(auditLogDirector);
        urlResolver.setOpenshiftApiSupplier(ignored -> openshiftApi);
    }

    @Test
    public void shouldPrometheusUrlFromProviderPropertiesBeUsedWhenDefined() {
        // given
        KubevirtProviderProperties properties = new KubevirtProviderProperties();
        properties.setPrometheusUrl(HTTPS_URL);
        given(provider.getAdditionalProperties()).willReturn(properties);

        // when
        String prometheusUrl = urlResolver.fetchPrometheusUrl(provider);

        // then
        assertEquals(HTTPS_URL, prometheusUrl);
    }

    @Test
    public void shouldPrometheusUrlBeFetchedWhenNotProvidedInProperties() throws ApiException {
        // given
        given(provider.getUrl()).willReturn("http://test.cluster.example.org");
        KubevirtProviderProperties properties = new KubevirtProviderProperties();
        given(provider.getAdditionalProperties()).willReturn(properties);

        V1RouteSpec spec = new V1RouteSpec();
        spec.setHost(TEST_HOST);
        V1Route route = new V1Route();
        route.setSpec(spec);
        V1RouteList routes = new V1RouteList().items(Lists.newArrayList(route));
        given(openshiftApi.listNamespacedRoute("openshift-monitoring",
                null,
                "metadata.name=prometheus-k8s",
                null,
                null,
                null,
                null,
                null,
                Boolean.FALSE)).willReturn(routes);

        // when
        String prometheusUrl = urlResolver.fetchPrometheusUrl(provider);

        // then
        assertEquals(HTTPS_URL, prometheusUrl);
    }

    @Test
    public void shouldReturnNullWhenNoPrometheusUrlCouldBeResolved() throws ApiException {
        // given
        given(provider.getUrl()).willReturn("http://test.cluster.example.org");
        KubevirtProviderProperties properties = new KubevirtProviderProperties();
        given(provider.getAdditionalProperties()).willReturn(properties);

        given(openshiftApi.listNamespacedRoute("openshift-monitoring",
                null,
                "metadata.name=prometheus-k8s",
                null,
                null,
                null,
                null,
                null,
                Boolean.FALSE)).willReturn(new V1RouteList());

        // when
        String prometheusUrl = urlResolver.fetchPrometheusUrl(provider);

        // then
        assertNull(prometheusUrl);
    }

    @Test
    public void shouldResetOngoingFailuresWhenOpenShiftConnectionFixed() throws IllegalAccessException, ApiException {
        // when
        given(provider.getUrl()).willReturn("http://test.cluster.example.org");
        given(openshiftApi.listNamespacedRoute("openshift-monitoring",
                null,
                "metadata.name=prometheus-k8s",
                null,
                null,
                null,
                null,
                null,
                Boolean.FALSE)).willReturn(new V1RouteList());

        KubevirtProviderProperties properties = new KubevirtProviderProperties();
        given(provider.getAdditionalProperties()).willReturn(properties);

        urlResolver.setOpenshiftApiSupplier(ignored -> {
            throw new IOException("Test failure");
        });

        // first lets simulate failure
        assertNull(urlResolver.fetchPrometheusUrl(provider));
        ConcurrentMap<String, LocalDateTime> failures = loadMapByName("openshiftConnectFailures");
        assertEquals(1, failures.size());

        // next simulate restoring connectivity
        urlResolver.setOpenshiftApiSupplier(ignored -> openshiftApi);

        // when
        urlResolver.fetchPrometheusUrl(provider);

        // then
        assertEquals(0, failures.size());
    }

    @Test
    public void shouldResetOngoingFailuresWhenCannotFetchRoutes() throws ApiException, IllegalAccessException {
        // given
        given(provider.getUrl()).willReturn("http://test.cluster.example.org");
        V1RouteSpec spec = new V1RouteSpec();
        spec.setHost(TEST_HOST);
        V1Route route = new V1Route();
        route.setSpec(spec);
        V1RouteList routes = new V1RouteList().items(Lists.newArrayList(route));
        given(openshiftApi.listNamespacedRoute("openshift-monitoring",
                null,
                "metadata.name=prometheus-k8s",
                null,
                null,
                null,
                null,
                null,
                Boolean.FALSE))
                        // first lets simulate failure with first call
                        .willThrow(new ApiException("Test failure"))
                        // fix it with the second call
                        .willReturn(routes);

        KubevirtProviderProperties properties = new KubevirtProviderProperties();
        given(provider.getAdditionalProperties()).willReturn(properties);

        ConcurrentMap<String, LocalDateTime> failures = loadMapByName("prometheusUrlRetrieveFailures");
        assertTrue(failures.isEmpty());

        // first failed call
        urlResolver.fetchPrometheusUrl(provider);
        assertEquals(1, failures.size());

        // second fixed call
        urlResolver.fetchPrometheusUrl(provider);
        assertEquals(0, failures.size());
    }

    @Test
    public void shouldResetOngoingFailuresWhenInvalidPromUri() throws ApiException, IllegalAccessException {
        // given
        given(provider.getUrl()).willReturn("http://test.cluster.example.org");
        V1RouteSpec spec = new V1RouteSpec();
        spec.setHost(TEST_HOST);
        V1Route route = new V1Route();
        route.setSpec(spec);

        V1RouteSpec invalidUriSpec = new V1RouteSpec();
        invalidUriSpec.setHost("- invalid uri -");
        V1Route invalidUriRoute = new V1Route();
        invalidUriRoute.setSpec(invalidUriSpec);

        given(openshiftApi.listNamespacedRoute("openshift-monitoring",
                null,
                "metadata.name=prometheus-k8s",
                null,
                null,
                null,
                null,
                null,
                Boolean.FALSE))
                        // first lets simulate failure with first call
                        .willReturn(new V1RouteList().items(Lists.newArrayList(invalidUriRoute)))
                        // fix it with the second call
                        .willReturn(new V1RouteList().items(Lists.newArrayList(route)));

        KubevirtProviderProperties properties = new KubevirtProviderProperties();
        given(provider.getAdditionalProperties()).willReturn(properties);

        ConcurrentMap<String, LocalDateTime> failures = loadMapByName("prometheusUrlByRouteRetrieveFailures");
        assertTrue(failures.isEmpty());

        // first failed call
        urlResolver.fetchPrometheusUrl(provider);
        assertEquals(1, failures.size());

        // second fixed call
        urlResolver.fetchPrometheusUrl(provider);
        assertEquals(0, failures.size());
    }

    @Test
    public void shouldResetOngoingFailuresWhenCoundFindAnyRoutes() throws ApiException, IllegalAccessException {
        // given
        given(provider.getUrl()).willReturn("http://test.cluster.example.org");
        V1RouteSpec spec = new V1RouteSpec();
        spec.setHost(TEST_HOST);
        V1Route route = new V1Route();
        route.setSpec(spec);

        given(openshiftApi.listNamespacedRoute("openshift-monitoring",
                null,
                "metadata.name=prometheus-k8s",
                null,
                null,
                null,
                null,
                null,
                Boolean.FALSE))
                        // first lets simulate failure with first call
                        .willReturn(new V1RouteList().items(Lists.newArrayList()))
                        // fix it with the second call
                        .willReturn(new V1RouteList().items(Lists.newArrayList(route)));

        KubevirtProviderProperties properties = new KubevirtProviderProperties();
        given(provider.getAdditionalProperties()).willReturn(properties);

        ConcurrentMap<String, LocalDateTime> failures = loadMapByName("prometheusUrlRouteNotFoundFailures");
        assertTrue(failures.isEmpty());

        // first failed call
        urlResolver.fetchPrometheusUrl(provider);
        assertEquals(1, failures.size());

        // second fixed call
        urlResolver.fetchPrometheusUrl(provider);
        assertEquals(0, failures.size());
    }

    private ConcurrentMap<String, LocalDateTime> loadMapByName(String failuresMapName) throws IllegalAccessException {
        return (ConcurrentHashMap<String, LocalDateTime>) FieldUtils
                .readDeclaredField(urlResolver, failuresMapName, true);
    }

}
