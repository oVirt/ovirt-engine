package org.ovirt.engine.core.bll.provider.network.openstack;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;

import com.woorea.openstack.base.client.OpenStackTokenProvider;
import com.woorea.openstack.quantum.Quantum;

public class ExternalNetworkProviderProxy extends BaseNetworkProviderProxy<OpenstackNetworkProviderProperties> {
    public ExternalNetworkProviderProxy(Provider<OpenstackNetworkProviderProperties> provider) {
        super(provider);
    }

    @Override
    protected void setClientTokenProvider(Quantum client) {
        if (StringUtils.isEmpty(getProvider().getAdditionalProperties().getTenantName())) {
            OpenStackTokenProvider tokenProvider = new ExternalNetworkTokenProvider(getProvider());
            client.setTokenProvider(tokenProvider);
        } else {
            super.setClientTokenProvider(client);
        }
    }

    @Override
    public String add(Network network) {
        testProviderIsNotReadOnly();
        return super.add(network);
    }

    @Override
    public void remove(String id) {
        testProviderIsNotReadOnly();
        super.remove(id);
    }

    @Override
    public void addSubnet(ExternalSubnet subnet) {
        testProviderIsNotReadOnly();
        super.addSubnet(subnet);
    }

    @Override
    public void removeSubnet(String id) {
        testProviderIsNotReadOnly();
        super.removeSubnet(id);
    }

    private boolean isReadOnly(){
        return getProvider().getAdditionalProperties().getReadOnly();
    }

    private void testProviderIsNotReadOnly() {
        if (isReadOnly()){
            throw new EngineException(EngineError.NO_IMPLEMENTATION);
        }
    }
}
