package org.ovirt.engine.core.bll.provider.network.openstack;

import org.ovirt.engine.core.common.businessentities.OpenStackProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;

import com.woorea.openstack.base.client.OpenStackTokenProvider;
import com.woorea.openstack.keystone.utils.KeystoneTokenProvider;

public class OpenStackTokenProviderFactory {
    public static <P extends OpenStackProviderProperties> OpenStackTokenProvider create(Provider<P> provider) {
        if (isApiV3(provider)) {
            return new KeystoneV3TokenProvider<>(provider);
        } else {
            String tenantName = provider.getAdditionalProperties().getTenantName();
            KeystoneTokenProvider keystoneTokenProvider =
                    new KeystoneTokenProvider(provider.getAuthUrl(),
                            provider.getUsername(),
                            provider.getPassword());
            return keystoneTokenProvider.getProviderByTenant(tenantName);
        }
    }

    public static boolean isApiV3(Provider provider) {
        String authUrl = provider.getAuthUrl();
        return authUrl != null && (authUrl.endsWith("/v3") || authUrl.endsWith("/v3/"));
    }
}
