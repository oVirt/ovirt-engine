package org.ovirt.engine.core.bll.provider.network.openstack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.provider.BaseProviderProxy;
import org.ovirt.engine.core.bll.provider.network.NetworkProviderProxy;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet.IpVersion;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.woorea.openstack.base.client.HttpMethod;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.quantum.Quantum;
import com.woorea.openstack.quantum.model.Networks;
import com.woorea.openstack.quantum.model.Port;
import com.woorea.openstack.quantum.model.Port.Binding;
import com.woorea.openstack.quantum.model.Subnet;
import com.woorea.openstack.quantum.model.Subnets;

public abstract class BaseNetworkProviderProxy<P extends OpenstackNetworkProviderProperties>
        extends BaseProviderProxy implements NetworkProviderProxy {

    private static final List<String> DEFAULT_SECURITY_GROUP = null;

    private static final List<String> NO_SECURITY_GROUPS = Collections.emptyList();

    private static final String SECURITY_GROUPS_PROPERTY = "SecurityGroups";

    private static final String API_VERSION = "/v2.0";

    protected static final String DEVICE_OWNER = "oVirt";

    private static final String FLAT_NETWORK = "flat";

    private static final String VLAN_NETWORK = "vlan";

    private Quantum client;

    private static Logger log = LoggerFactory.getLogger(BaseNetworkProviderProxy.class);

    public BaseNetworkProviderProxy(Provider<P> provider) {
        super(provider);
    }

    private Quantum getClient() {
        if (client == null) {
            client = new Quantum(getProvider().getUrl() + API_VERSION, new CustomizedRESTEasyConnector());
            if (getProvider().isRequiringAuthentication()) {
                setClientTokenProvider(client);
            }
        }
        return client;
    }

    protected void setClientTokenProvider(Quantum client) {
        client.setTokenProvider(OpenStackTokenProviderFactory.create(getProvider()));
    }

    @Override
    public String add(Network network) {
        com.woorea.openstack.quantum.model.Network networkForCreate = createNewNetworkEntity(network);
        com.woorea.openstack.quantum.model.Network createdNetwork =
                execute(getClient().networks().create(networkForCreate));
        return createdNetwork.getId();
    }

    protected com.woorea.openstack.quantum.model.Network createNewNetworkEntity(Network network) {
        com.woorea.openstack.quantum.model.Network networkForCreate = new com.woorea.openstack.quantum.model.Network();
        networkForCreate.setAdminStateUp(true);
        networkForCreate.setName(network.getName());
        if (!network.isDefaultMtu()) {
            networkForCreate.setMtu(network.getMtu());
        }

        if (network.getProvidedBy().hasCustomPhysicalNetworkName()) {
            networkForCreate.setProviderPhysicalNetwork(network.getProvidedBy().getCustomPhysicalNetworkName());
            if (network.getProvidedBy().hasExternalVlanId()) {
                networkForCreate.setProviderNetworkType(VLAN_NETWORK);
                networkForCreate.setProviderSegmentationId(network.getProvidedBy().getExternalVlanId());
            } else {
                networkForCreate.setProviderNetworkType(FLAT_NETWORK);
            }
        }
        if (!getProvider().isRequiringAuthentication()) {
            networkForCreate.setTenantId(DEVICE_OWNER);
        }
        networkForCreate.setPortSecurityEnabled(network.getProvidedBy().getPortSecurityEnabled());
        return networkForCreate;
    }

    @Override
    public void remove(String id) {
            execute(getClient().networks().delete(id));
    }

    @Override
    public Network get(String id) {
        com.woorea.openstack.quantum.model.Network externalNetwork =
                execute(getClient().networks().show(id));
        return map(externalNetwork);
    }

    @Override
    public List<Network> getAll() {
        Networks networks = execute(getClient().networks().list());
        return map(networks.getList());
    }

    @Override
    public List<ExternalSubnet> getAllSubnets(ProviderNetwork network) {
        List<ExternalSubnet> result = new ArrayList<>();
        Subnets subnets = execute(getClient().subnets().list());
        for (Subnet subnet : subnets.getList()) {
            if (network.getExternalId().equals(subnet.getNetworkId())) {
                result.add(map(subnet, network));
            }
        }

        return result;
    }

    private ExternalSubnet map(Subnet subnet, ProviderNetwork network) {
        ExternalSubnet s = new ExternalSubnet();
        s.setId(subnet.getId());
        s.setCidr(subnet.getCidr());
        s.setIpVersion(Subnet.IpVersion.IPV6.equals(subnet.getIpversion())
                ? IpVersion.IPV6
                : IpVersion.IPV4);
        s.setName(subnet.getName());
        s.setExternalNetwork(network);
        s.setGateway(subnet.getGw());
        s.setDnsServers(subnet.getDnsNames());
        return s;
    }

    @Override
    public void addSubnet(ExternalSubnet subnet) {
        com.woorea.openstack.quantum.model.Network externalNetwork = getExternalNetwork(subnet.getExternalNetwork());
        Subnet subnetForCreate = createNewSubnetEntity(subnet, externalNetwork);
        execute(getClient().subnets().create(subnetForCreate));
    }

    protected Subnet createNewSubnetEntity(ExternalSubnet subnet,
            com.woorea.openstack.quantum.model.Network externalNetwork) {
        Subnet subnetForCreate = new Subnet();
        subnetForCreate.setCidr(subnet.getCidr());
        subnetForCreate.setIpversion(subnet.getIpVersion() == IpVersion.IPV6
                ? Subnet.IpVersion.IPV6 : Subnet.IpVersion.IPV4);
        subnetForCreate.setName(subnet.getName());
        subnetForCreate.setNetworkId(externalNetwork.getId());
        subnetForCreate.setEnableDHCP(true);
        subnetForCreate.setGw(subnet.getGateway());
        subnetForCreate.setDnsNames(subnet.getDnsServers());
        subnetForCreate.setTenantId(externalNetwork.getTenantId());
        return subnetForCreate;
    }

    @Override
    public void removeSubnet(String id) {
        execute(getClient().subnets().delete(id));
    }

    @Override
    public void testConnection() {
        execute(new OpenStackRequest<>(getClient(), HttpMethod.GET, "/", null, ApiRootResponse.class));
    }

    private List<Network> map(List<com.woorea.openstack.quantum.model.Network> externalNetworks) {
        List<Network> networks = new ArrayList<>(externalNetworks.size());

        for (com.woorea.openstack.quantum.model.Network externalNetwork : externalNetworks) {
            networks.add(map(externalNetwork));
        }

        return networks;
    }

    private Network map(com.woorea.openstack.quantum.model.Network externalNetwork) {
        Network network = new Network();
        network.setVmNetwork(true);
        network.setProvidedBy(new ProviderNetwork(getProvider().getId(), externalNetwork.getId()));
        network.setName(externalNetwork.getName());
        if (externalNetwork.getMtu() != null) {
            network.setMtu(externalNetwork.getMtu());
        }

        mapPhysicalNetworkParameters(externalNetwork, network);
        network.getProvidedBy().setPortSecurityEnabled(externalNetwork.getPortSecurityEnabled());

        return network;
    }

    private void mapPhysicalNetworkParameters(com.woorea.openstack.quantum.model.Network externalNetwork,
            Network network) {
        String providerNetworkType = externalNetwork.getProviderNetworkType();
        ProviderNetwork providerNetwork = network.getProvidedBy();
        providerNetwork.setExternalVlanId(VLAN_NETWORK.equals(providerNetworkType) ?
                externalNetwork.getProviderSegmentationId() :
                null);
        providerNetwork.setCustomPhysicalNetworkName(externalNetwork.getProviderPhysicalNetwork());
        providerNetwork.setProviderNetworkType(providerNetworkType);
    }

    @Override
    public Map<String, String> allocate(
        Network network, VnicProfile vnicProfile, VmNic nic, VDS host,
        boolean ignoreSecurityGroupsOnUpdate, String hostBindingId) {

        if (hostBindingId==null) {
            hostBindingId = host.getHostName();
            log.warn("Host binding id for external network {} on host {} is null, using host id {} to allocate vNIC " +
                    " {} instead. Please provide an after_get_caps hook for the plugin type {} on host {}",
                network.getName(), host.getName(), hostBindingId, nic.getName(),
                getProvider().getAdditionalProperties().getPluginType() , host.getName());
        }

        Port port = locatePort(nic);
        List<String> securityGroups = getSecurityGroups(vnicProfile);
        if (port == null) {
            com.woorea.openstack.quantum.model.Network externalNetwork =
                    getExternalNetwork(network.getProvidedBy());
            Port portForCreate = createNewPortForAllocate(nic,
                securityGroups, hostBindingId, externalNetwork);
            port = execute(getClient().ports().create(portForCreate));
        } else {
            boolean securityGroupsChanged = !ignoreSecurityGroupsOnUpdate &&
                securityGroupsChanged(port.getSecurityGroups(), securityGroups);
            updatePort(port, securityGroupsChanged, securityGroups, hostBindingId, nic);
        }
        Map<String, String> runtimeProperties = createPortAllocationRuntimeProperties(port);

        return runtimeProperties;
    }

    private Port updatePort(
        Port port, boolean securityGroupsChanged, List<String> securityGroups, String hostBindingId,
        VmNic nic) {
        boolean hostChanged = hostChanged(port, hostBindingId);
        if (securityGroupsChanged || hostChanged) {
            List<String> modifiedSecurityGroups = securityGroupsChanged ?
                securityGroups : port.getSecurityGroups();
            Port portForUpdate = modifyPortForAllocate(
                port, hostBindingId, hostChanged, securityGroupsChanged, modifiedSecurityGroups, nic.getMacAddress());
            return execute(getClient().ports().update(portForUpdate));
        }
        return port;
    }

    protected Port modifyPortForAllocate(Port port, String hostBindingId, boolean hostChanged,
                                         boolean securityGroupsChanged, List<String> modifiedSecurityGroups,
                                         String macAddress) {
        Port portForUpdate = securityGroupsChanged ? new PortForUpdate() : new Port();
        portForUpdate.setId(port.getId());

        if (securityGroupsChanged) {
            portForUpdate.setSecurityGroups(modifiedSecurityGroups);
        }

        if (hostChanged) {
            portForUpdate.setBinding(new Binding());
            portForUpdate.getBinding().setHostId(hostBindingId);
            portForUpdate.setMacAddress(macAddress);
        }
        return portForUpdate;
    }

    protected Port createNewPortForAllocate(VmNic nic,
            List<String> securityGroups, String hostBindingId,
            com.woorea.openstack.quantum.model.Network externalNetwork) {
        Port portForCreate = new Port();
        portForCreate.setAdminStateUp(true);
        portForCreate.setName(nic.getName());
        portForCreate.setMacAddress(nic.getMacAddress());
        portForCreate.setNetworkId(externalNetwork.getId());
        portForCreate.setDeviceOwner(DEVICE_OWNER);
        portForCreate.setDeviceId(nic.getId().toString());
        portForCreate.setSecurityGroups(securityGroups);
        portForCreate.setBinding(new Binding());
        portForCreate.getBinding().setHostId(hostBindingId);
        portForCreate.setTenantId(externalNetwork.getTenantId());
        return portForCreate;
    }

    protected Map<String, String> createPortAllocationRuntimeProperties(Port port) {
        Map<String, String> runtimeProperties = new HashMap<>();
        runtimeProperties.put("vnic_id", port.getId());
        String providerType = getProvider().getType().name();
        runtimeProperties.put("provider_type", providerType);
        if (port.getSecurityGroups() != null && !port.getSecurityGroups().isEmpty()) {
            runtimeProperties.put("security_groups", StringUtils.join(port.getSecurityGroups(), ','));
        }
        runtimeProperties.put("plugin_type", StringUtils.defaultString(
            getProvider().getAdditionalProperties().getPluginType()));
        return runtimeProperties;
    }

    private com.woorea.openstack.quantum.model.Network getExternalNetwork(ProviderNetwork providerNetwork) {
        return execute(getClient().networks().show(providerNetwork.getExternalId()));
    }

    private boolean hostChanged(Port port, String hostId) {
        return port.getBinding() == null || hostId == null ||
            !StringUtils.equals(port.getBinding().getHostId(), hostId);
    }

    private boolean securityGroupsChanged(List<String> existingSecurityGroups, List<String> desiredSecurityGroups) {
        existingSecurityGroups = existingSecurityGroups == null ? NO_SECURITY_GROUPS : existingSecurityGroups;
        return (desiredSecurityGroups == DEFAULT_SECURITY_GROUP
                && existingSecurityGroups.isEmpty())
                || (desiredSecurityGroups != DEFAULT_SECURITY_GROUP
                && !CollectionUtils.isEqualCollection(existingSecurityGroups, desiredSecurityGroups));
    }

    private List<String> getSecurityGroups(VnicProfile vnicProfile) {
        Map<String, String> customProperties = vnicProfile.getCustomProperties();

        if (customProperties.containsKey(SECURITY_GROUPS_PROPERTY)) {
            String securityGroupsString = customProperties.get(SECURITY_GROUPS_PROPERTY);

            if (StringUtils.isEmpty(securityGroupsString)) {
                return NO_SECURITY_GROUPS;
            }

            return Arrays.asList(securityGroupsString.split(",\\s*"));
        }

        return DEFAULT_SECURITY_GROUP;
    }

    @Override
    public void deallocate(VmNic nic) {
        Port port = locatePort(nic);

        if (port != null) {
            execute(getClient().ports().delete(port.getId()));
        }
    }

    private Port locatePort(VmNic nic) {
        List<Port> ports = execute(getClient().ports().list()).getList();
        for (Port port : ports) {
            if (DEVICE_OWNER.equals(port.getDeviceOwner()) && nic.getId().toString().equals(port.getDeviceId())) {
                return port;
            }
        }

        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ApiRootResponse {
        // No implementation since we don't care what's inside the response, just that it succeeded.
    }

    @Override
    public Provider<P> getProvider() {
        return (Provider<P>)super.getProvider();
    }

    private <R> R execute(OpenStackRequest<R> request) {
        try {
            return request.execute();
        } catch (OpenStackResponseException e) {
            throw new EngineException(EngineError.PROVIDER_FAILURE, e, true);
        } catch (RuntimeException e) {
            throw new EngineException(EngineError.PROVIDER_FAILURE, ExceptionUtils.getRootCause(e), true);
        }
    }
}
