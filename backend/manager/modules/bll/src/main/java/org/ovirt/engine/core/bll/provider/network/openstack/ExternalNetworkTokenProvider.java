package org.ovirt.engine.core.bll.provider.network.openstack;

import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;

import com.woorea.openstack.base.client.OpenStackTokenProvider;
import com.woorea.openstack.keystone.Keystone;
import com.woorea.openstack.keystone.model.Access;
import com.woorea.openstack.keystone.model.Token;
import com.woorea.openstack.keystone.model.authentication.UsernamePassword;

public class ExternalNetworkTokenProvider implements OpenStackTokenProvider {

    private final Provider<OpenstackNetworkProviderProperties> provider;
    private Access access;

    public ExternalNetworkTokenProvider(Provider<OpenstackNetworkProviderProperties> provider) {
        this.provider = provider;
    }

    @Override
    public String getToken() {
        if (access == null) {
            Keystone keystone = createKeystone(provider.getAuthUrl());
            UsernamePassword usernamePassword =
                    new UsernamePassword(provider.getUsername(), provider.getPassword());
            access = executeKeystoneQuery(keystone, usernamePassword);
        }
        return getTokenIdFromAccess(access);
    }

    @Override
    public void expireToken() {
        access = null;
    }

    private Access executeKeystoneQuery(Keystone keystone, UsernamePassword usernamePassword) {
        return keystone.tokens().authenticate(usernamePassword).execute();
    }

    private String getTokenIdFromAccess(Access access) {
        final Token token = access.getToken();
        return getTokenId(token);
    }

    String getTokenId(Token token) {
        return token.getId();
    }

    Keystone createKeystone(String authUrl) {
        return new Keystone(authUrl, new CustomizedRESTEasyConnector());
    }
}
