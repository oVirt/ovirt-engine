package org.ovirt.engine.core.vdsbroker;

import static org.ovirt.engine.core.utils.KubevirtHelper.getConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.vdsbroker.vdsbroker.PrometheusClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import kubevirt.io.KubevirtApi;
import openshift.io.OpenshiftApi;

public class KubevirtUtils {
    public static String COMMENT_ANNOTATION = "kubevirt.io/comment";
    public static String DESCRIPTION_ANNOTATION = "kubevirt.io/description";

    private static Logger log = LoggerFactory.getLogger(KubevirtUtils.class);

    public static PrometheusClient create(Provider provider) {
        String promUrl = ((KubevirtProviderProperties)provider.getAdditionalProperties()).getPrometheusUrl();
        if (promUrl == null) {
            promUrl = PrometheusClient.fetchPrometheusUrl(provider);
            if (promUrl == null) {
                log.error(
                    "No prometheus URL provided. Statistics won't be fetched for provider '{}'", provider.getName()
                );
                return null;
            }
        }
        if (promUrl.startsWith("https")) {
            return new PrometheusClient(provider, promUrl, PrometheusClient.getContext(provider));
        } else {
            return new PrometheusClient(provider, promUrl);
        }
    }

    public static CoreV1Api getCoreApi(Provider<?> provider) throws IOException {
        ApiClient client = createApiClient(provider);
        return new CoreV1Api(client);
    }

    public static KubevirtApi getKubevirtApi(Provider provider) throws IOException {
        ApiClient client = createApiClient(provider);
        return new KubevirtApi(client);
    }

    public static OpenshiftApi getOpenshiftApi(Provider provider) throws IOException {
        ApiClient client = createApiClient(provider);
        return new OpenshiftApi(client);
    }

    public static ApiClient createApiClient(Provider provider) throws IOException {
        KubevirtProviderProperties additionalProperties =
                (KubevirtProviderProperties) provider.getAdditionalProperties();
        String certificateAuthority = additionalProperties.getCertificateAuthority();
        if (StringUtils.isNotEmpty(certificateAuthority)) {
            certificateAuthority = additionalProperties.getCertificateAuthority()
                    .replace("-----BEGIN CERTIFICATE-----\n", "")
                    .replace("-----END CERTIFICATE-----", ""); // need for PEM format cert string
        }

        String token = provider.getPassword();
        String url = provider.getUrl();
        Map<String, Object> config = getConfig(url, token, certificateAuthority);
        KubeConfig kubeConfig = new KubeConfig((ArrayList<Object>) config.get("contexts"),
                (ArrayList<Object>) config.get("clusters"),
                (ArrayList<Object>) config.get("users"));
        kubeConfig.setContext((String) config.get("name"));
        return ClientBuilder.kubeconfig(kubeConfig).build();
    }
}
