package org.ovirt.engine.core.bll.provider.network.openstack;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;

import com.woorea.openstack.keystone.utils.KeystoneTokenProvider;
import com.woorea.openstack.quantum.Quantum;
import com.woorea.openstack.quantum.model.Port;
import com.woorea.openstack.quantum.model.Subnet;

public class OpenstackNetworkProviderProxy extends BaseNetworkProviderProxy<OpenstackNetworkProviderProperties> {

    public OpenstackNetworkProviderProxy(Provider<OpenstackNetworkProviderProperties> provider) {
        super(provider);
    }

    @Override
    protected void setClientTokenProvider(Quantum client) {
        String tenantName = provider.getAdditionalProperties().getTenantName();
        KeystoneTokenProvider keystoneTokenProvider =
                new KeystoneTokenProvider(provider.getAuthUrl(),
                        provider.getUsername(),
                        provider.getPassword());
        client.setTokenProvider(keystoneTokenProvider.getProviderByTenant(tenantName));
    }

    @Override
    protected Map<String, String> createPortAllocationRuntimeProperties(Port port) {
        Map<String, String> runtimeProperties = super.createPortAllocationRuntimeProperties(port);
        runtimeProperties.put("plugin_type", provider.getAdditionalProperties().getPluginType());
        return runtimeProperties;
    }

   @Override
   protected Port createNewPortForAllocate(VmNic nic,
            List<String> securityGroups, String hostId,
            com.woorea.openstack.quantum.model.Network externalNetwork) {
        Port portForCreate = super.createNewPortForAllocate(nic,
                securityGroups, hostId, externalNetwork);
        portForCreate.setTenantId(externalNetwork.getTenantId());
        return portForCreate;
    }

    @Override
    protected Subnet createNewSubnetEntity(ExternalSubnet subnet,
            com.woorea.openstack.quantum.model.Network externalNetwork) {
        Subnet subnetForCreate = super.createNewSubnetEntity(subnet, externalNetwork);
        subnetForCreate.setTenantId(externalNetwork.getTenantId());
        return subnetForCreate;
    }

    @Override
    protected com.woorea.openstack.quantum.model.Network createNewNetworkEntity(Network network) {
        com.woorea.openstack.quantum.model.Network networkForCreate =
                super.createNewNetworkEntity(network);
        if (!provider.isRequiringAuthentication()) {
            networkForCreate.setTenantId(DEVICE_OWNER);
        }
        return networkForCreate;
    }
}
