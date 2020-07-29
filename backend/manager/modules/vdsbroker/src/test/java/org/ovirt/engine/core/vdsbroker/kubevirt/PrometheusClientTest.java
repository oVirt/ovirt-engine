package org.ovirt.engine.core.vdsbroker.kubevirt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;

@ExtendWith(MockitoExtension.class)
class PrometheusClientTest {

    private static final String HTTP_URL = "http://prometheus.test.example.org";
    private static final String HTTPS_URL = "https://prometheus.test.example.org";
    @Mock
    private Provider<KubevirtProviderProperties> provider;

    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private PrometheusUrlResolver urlResolver;

    @Test
    public void shouldNotCreateClientWhenNoPrometheusUrlCanBeResolved() {
        // given
        given(urlResolver.fetchPrometheusUrl(any(Provider.class))).willReturn(null);

        // when
        PrometheusClient client = PrometheusClient.create(provider, urlResolver);

        // then
        assertNull(client);
    }

    @Test
    public void shouldHttpPrometheusClientBeCreated() {
        // given
        given(urlResolver.fetchPrometheusUrl(any(Provider.class))).willReturn(HTTP_URL);

        // when
        PrometheusClient client = PrometheusClient.create(provider, urlResolver);

        // then
        assertNotNull(client);
    }

    @Test
    public void shouldHttpsPrometheusClientBeCreated() {
        // given
        given(urlResolver.fetchPrometheusUrl(any(Provider.class))).willReturn(HTTPS_URL);
        given(provider.getAdditionalProperties()).willReturn(new KubevirtProviderProperties());

        // when
        PrometheusClient client = PrometheusClient.create(provider, urlResolver);

        // then
        assertNotNull(client);
    }

    @Test
    public void shouldFetchAMetric() throws IOException {
        // given
        given(urlResolver.fetchPrometheusUrl(any(Provider.class))).willReturn(HTTP_URL);
        PrometheusClient client = PrometheusClient.create(provider, urlResolver);
        client.setHttpClient(httpClient);
        ArgumentCaptor<HttpUriRequest> request = ArgumentCaptor.forClass(HttpUriRequest.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        given(httpClient.execute(any(HttpUriRequest.class))).willReturn(response);
        StatusLine statusLine = mock(StatusLine.class);
        given(statusLine.getStatusCode()).willReturn(200);
        given(response.getStatusLine()).willReturn(statusLine);

        StringEntity entity = new StringEntity("{\n" +
                "  \"status\": \"success\",\n" +
                "  \"data\" : {\n" +
                "    \"result\" : [\n" +
                "      {\n" +
                "        \"value\" : [\"irrelevant\", \"12.40\"]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}");

        given(response.getEntity()).willReturn(entity);
        String testNode = "testNode1";

        // when
        Double result = client.getNodeCpuIdle(testNode);

        // then
        verify(httpClient).execute(request.capture());
        HttpUriRequest capturedRequest = request.getValue();
        assertEquals("POST", capturedRequest.getMethod());
        String expectedUri = HTTP_URL + "/api/v1/query?query="
                + URLEncoder.encode(String.format(
                        "avg by (mode) (irate(node_cpu_seconds_total{instance='%1$s', mode='idle'}[1m])) * 100",
                        testNode), StandardCharsets.UTF_8);
        assertEquals(expectedUri, capturedRequest.getURI().toString());
        assertEquals(12.40, result.doubleValue());

    }

}
