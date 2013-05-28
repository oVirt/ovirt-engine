package org.ovirt.engine.core.bll.provider.network.openstack;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;

import com.woorea.openstack.base.client.HttpMethod;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.keystone.utils.KeystoneTokenProvider;
import com.woorea.openstack.quantum.Quantum;
import com.woorea.openstack.quantum.model.NetworkForCreate;
import com.woorea.openstack.quantum.model.Networks;
import com.woorea.openstack.quantum.model.Port;
import com.woorea.openstack.quantum.model.PortForCreate;

public class OpenstackNetworkProviderProxy implements NetworkProviderProxy {

    private static final String API_VERSION = "/v2.0";

    private static final String DEVICE_OWNER = "oVirt";

    private static final String FLAT_NETWORK = "flat";

    private static final String VLAN_NETWORK = "vlan";

    private Provider<OpenstackNetworkProviderProperties> provider;

    private Quantum client;

    public OpenstackNetworkProviderProxy(Provider<OpenstackNetworkProviderProperties> provider) {
        this.provider = provider;
    }

    private Quantum getClient() {
        if (client == null) {
            client = new Quantum(provider.getUrl() + API_VERSION);
            if (provider.isRequiringAuthentication()) {
                final String tenantName = provider.getAdditionalProperties().getTenantName();
                final KeystoneTokenProvider keystoneTokenProvider =
                        new KeystoneTokenProvider(Config.<String> GetValue(ConfigValues.KeystoneAuthUrl),
                                provider.getUsername(),
                                provider.getPassword());

                client.setTokenProvider(keystoneTokenProvider.getProviderByTenant(tenantName));
            }
        }

        return client;
    }

    @Override
    public String add(Network network) {
        NetworkForCreate networkForCreate = new NetworkForCreate();
        networkForCreate.setAdminStateUp(true);
        networkForCreate.setName(network.getName());
        if (network.getLabel() != null) {
            networkForCreate.setProviderPhysicalNetwork(network.getLabel());
            if (network.getVlanId() == null) {
                networkForCreate.setProviderNetworkType(FLAT_NETWORK);
            } else {
                networkForCreate.setProviderNetworkType(VLAN_NETWORK);
                networkForCreate.setProviderSegmentationId(network.getVlanId());
            }
        }

        try {
            com.woorea.openstack.quantum.model.Network createdNetwork =
                    getClient().networks().create(networkForCreate).execute();
            return createdNetwork.getId();
        } catch (RuntimeException e) {
            throw new VdcBLLException(VdcBllErrors.PROVIDER_FAILURE, e);
        }
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
            network.setLabel(externalNetwork.getProviderPhyNet());
            if (VLAN_NETWORK.equals(externalNetwork.getNetType())) {
                network.setVlanId(Integer.valueOf(externalNetwork.getProviderSegID()));
            }
            networks.add(network);
        }

        return networks;
    }

    @Override
    public Map<String, String> allocate(Network network, VmNetworkInterface nic) {
        deallocate(nic);
        try {
            com.woorea.openstack.quantum.model.Network externalNetwork =
                    getClient().networks().show(network.getProvidedBy().getExternalId()).execute();
            PortForCreate port = new PortForCreate();
            port.setAdminStateUp(true);
            port.setName(nic.getName());
            port.setTenantId(externalNetwork.getTenantId());
            port.setMacAddress(nic.getMacAddress());
            port.setNetworkId(externalNetwork.getId());
            port.setDeviceOwner(DEVICE_OWNER);
            port.setDeviceId(nic.getId().toString());

            Port createdPort = getClient().ports().create(port).execute();

            Map<String, String> runtimeProperties = new HashMap<>();
            runtimeProperties.put("vnic_id", createdPort.getId());
            runtimeProperties.put("provider_type", provider.getType().name());
            runtimeProperties.put("plugin_type", provider.getAdditionalProperties().getPluginType().name());

            return runtimeProperties;
        } catch (RuntimeException e) {
            throw new VdcBLLException(VdcBllErrors.PROVIDER_FAILURE, e);
        }
    }

    @Override
    public void deallocate(VmNetworkInterface nic) {
        try {
            List<Port> ports = getClient().ports().list().execute().getList();
            for (Port port : ports) {
                if (DEVICE_OWNER.equals(port.getDeviceOwner()) && nic.getId().toString().equals(port.getDeviceId())) {
                    getClient().ports().delete(port.getId()).execute();
                }
            }
        } catch (RuntimeException e) {
            throw new VdcBLLException(VdcBllErrors.PROVIDER_FAILURE, e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ApiRootResponse {
        // No implementation since we don't care what's inside the response, just that it succeeded.
    }
}
