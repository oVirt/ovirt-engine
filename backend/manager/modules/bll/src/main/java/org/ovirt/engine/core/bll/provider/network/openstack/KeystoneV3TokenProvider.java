package org.ovirt.engine.core.bll.provider.network.openstack;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.OpenStackProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;

import com.woorea.openstack.base.client.OpenStackResponse;
import com.woorea.openstack.base.client.OpenStackTokenProvider;
import com.woorea.openstack.keystone.v3.Keystone;
import com.woorea.openstack.keystone.v3.model.Authentication;

public class KeystoneV3TokenProvider<P extends OpenStackProviderProperties> implements OpenStackTokenProvider {
    private static final String TOKEN_HEADER_NAME = "X-Subject-Token";
    private final Provider<P> provider;
    private String token;

    KeystoneV3TokenProvider(Provider<P> provider) {
        this.provider = provider;
    }

    @Override
    public void expireToken() {
        token = null;
    }

    @Override
    public String getToken() {
        if (token == null) {
            token =  createToken();
        }
        return token;
    }

    private String createToken() {
        OpenStackProviderProperties providerProperties = provider.getAdditionalProperties();

        Authentication auth = new Authentication();
        auth.setIdentity(Authentication.Identity.password(providerProperties.getUserDomainName(),
                provider.getUsername(), provider.getPassword()));
        if (StringUtils.isNotEmpty(providerProperties.getProjectName())) {
            auth.setScope(Authentication.Scope.project(providerProperties.getProjectDomainName(),
                    providerProperties.getProjectName()));
        }

        return getTokenId(requestToken(auth));
    }

    OpenStackResponse requestToken(Authentication auth) {
        return new Keystone(provider.getAuthUrl()).tokens().authenticate(auth).request();
    }

    private String getTokenId(OpenStackResponse response) {
        return response.header(TOKEN_HEADER_NAME);
    }
}
