package org.ovirt.engine.core.bll.provider.network.openstack;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.ovirt.engine.core.bll.provider.network.NetworkProviderProxy;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;

import com.woorea.openstack.base.client.HttpMethod;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.quantum.Quantum;
import com.woorea.openstack.quantum.model.Networks;

public class OpenstackNetworkProviderProxy implements NetworkProviderProxy {

    private static final String API_VERSION = "/v2.0";

    private Provider<OpenstackNetworkProviderProperties> provider;

    private Quantum client;

    public OpenstackNetworkProviderProxy(Provider<OpenstackNetworkProviderProperties> provider) {
        this.provider = provider;
    }

    private Quantum getClient() {
        if (client == null) {
            client = new Quantum(provider.getUrl() + API_VERSION);
        }

        return client;
    }

    @Override
    public List<Network> getAll() {
        try {
            Networks networks = getClient().networks().list().execute();
            return map(networks.getList());
        } catch (RuntimeException e) {
            throw new VdcBLLException(VdcBllErrors.PROVIDER_FAILURE, e);
        }
    }

    @Override
    public void testConnection() {
        try {
            getClient().execute(new OpenStackRequest<>(getClient(), HttpMethod.GET, "", null, ApiRootResponse.class));
        } catch (RuntimeException e) {
            throw new VdcBLLException(VdcBllErrors.PROVIDER_FAILURE, e);
        }
    }

    /**
     * Currently, SSL is not supported by Openstack Network Providers.
     */
    @Override
    public List<? extends Certificate> getCertificateChain() {
        throw new NotImplementedException();
    }

    private List<Network> map(List<com.woorea.openstack.quantum.model.Network> externalNetworks) {
        List<Network> networks = new ArrayList<>(externalNetworks.size());

        for (com.woorea.openstack.quantum.model.Network externalNetwork : externalNetworks) {
            Network network = new Network();
            network.setVmNetwork(true);
            network.setProvidedBy(new ProviderNetwork(provider.getId(), externalNetwork.getId()));
            network.setName(externalNetwork.getName());
            networks.add(network);
        }

        return networks;
    }

    @Override
    public Map<String, String> allocate(Network network, VmNetworkInterface nic) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deallocate(VmNetworkInterface nic) {
        // TODO Auto-generated method stub
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ApiRootResponse {
        // No implementation since we don't care what's inside the response, just that it succeeded.
    }
}
