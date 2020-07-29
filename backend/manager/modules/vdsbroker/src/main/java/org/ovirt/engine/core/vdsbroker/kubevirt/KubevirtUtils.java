package org.ovirt.engine.core.vdsbroker.kubevirt;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.console.ConsoleOptions;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import kubevirt.io.KubevirtApi;
import openshift.io.OpenshiftApi;

public class KubevirtUtils {
    public static String COMMENT_ANNOTATION = "kubevirt.io/comment";
    public static String DESCRIPTION_ANNOTATION = "kubevirt.io/description";

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
        Map<String, Object> config = KubevirtHelper.getConfig(url, token, certificateAuthority);
        KubeConfig kubeConfig = new KubeConfig((ArrayList<Object>) config.get("contexts"),
                (ArrayList<Object>) config.get("clusters"),
                (ArrayList<Object>) config.get("users"));
        kubeConfig.setContext((String) config.get("name"));
        return ClientBuilder.kubeconfig(kubeConfig).build();
    }

    public static void updateConsoleOptions(ConsoleOptions options, VM vm, Provider<KubevirtProviderProperties> provider) {
        try {
            URL url = new URL(provider.getUrl());
            // XXX: do we need cacert to pass?
            options.setHost(url.getHost());
            options.setPort(url.getPort());
            options.setPath("/apis/subresources.kubevirt.io/v1alpha3/namespaces/" + vm.getNamespace() + "/virtualmachineinstances/" + vm.getName() + "/vnc");
            options.setToken(provider.getPassword());
        } catch (MalformedURLException e) {
            // at this stage url is validated and we know it is correct
        }
    }
}
