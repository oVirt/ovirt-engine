package org.ovirt.engine.core.bll.provider.network.openstack;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
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
import org.ovirt.engine.core.utils.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woorea.openstack.base.client.HttpMethod;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.keystone.utils.KeystoneTokenProvider;
import com.woorea.openstack.quantum.Quantum;
import com.woorea.openstack.quantum.model.Networks;
import com.woorea.openstack.quantum.model.Port;
import com.woorea.openstack.quantum.model.Port.Binding;
import com.woorea.openstack.quantum.model.Subnet;
import com.woorea.openstack.quantum.model.Subnets;

public abstract class BaseNetworkProviderProxy<P extends OpenstackNetworkProviderProperties> implements NetworkProviderProxy {

    private static final List<String> DEFAULT_SECURITY_GROUP = null;

    private static final List<String> NO_SECURITY_GROUPS = Collections.emptyList();

    private static final String SECURITY_GROUPS_PROPERTY = "SecurityGroups";

    private static final String API_VERSION = "/v2.0";

    protected static final String DEVICE_OWNER = "oVirt";

    private static final String FLAT_NETWORK = "flat";

    private static final String VLAN_NETWORK = "vlan";

    protected final Provider<P> provider;

    private Quantum client;

    private static Logger log = LoggerFactory.getLogger(BaseNetworkProviderProxy.class);

    public BaseNetworkProviderProxy(Provider<P> provider) {
        this.provider = provider;
    }

    private Quantum getClient() {
        if (client == null) {
            client = new Quantum(provider.getUrl() + API_VERSION);
            if (provider.isRequiringAuthentication()) {
                setClientTokenProvider(client);
            }
        }
        return client;
    }

    protected void setClientTokenProvider(Quantum client) {
        String tenantName = provider.getAdditionalProperties().getTenantName();
        KeystoneTokenProvider keystoneTokenProvider =
                new KeystoneTokenProvider(provider.getAuthUrl(),
                        provider.getUsername(),
                        provider.getPassword());
        client.setTokenProvider(keystoneTokenProvider.getProviderByTenant(tenantName));
    }

    @Override
    public String add(Network network) {
        com.woorea.openstack.quantum.model.Network networkForCreate = createNewNetworkEntity(network);
        try {
            com.woorea.openstack.quantum.model.Network createdNetwork =
                    getClient().networks().create(networkForCreate).execute();
            return createdNetwork.getId();
        } catch (RuntimeException e) {
            throw new EngineException(EngineError.PROVIDER_FAILURE, e);
        }
    }

    protected com.woorea.openstack.quantum.model.Network createNewNetworkEntity(Network network) {
        com.woorea.openstack.quantum.model.Network networkForCreate = new com.woorea.openstack.quantum.model.Network();
        networkForCreate.setAdminStateUp(true);
        networkForCreate.setName(network.getName());

        if (NetworkUtils.isLabeled(network)) {
            networkForCreate.setProviderPhysicalNetwork(network.getLabel());
            if (NetworkUtils.isVlan(network)) {
                networkForCreate.setProviderNetworkType(VLAN_NETWORK);
                networkForCreate.setProviderSegmentationId(network.getVlanId());
            } else {
                networkForCreate.setProviderNetworkType(FLAT_NETWORK);
            }
        }
        if (!provider.isRequiringAuthentication()) {
            networkForCreate.setTenantId(DEVICE_OWNER);
        }
        return networkForCreate;
    }

    @Override
    public void remove(String id) {
        try {
            getClient().networks().delete(id).execute();
        } catch (RuntimeException e) {
            throw new EngineException(EngineError.PROVIDER_FAILURE, e);
        }
    }

    @Override
    public List<Network> getAll() {
        try {
            Networks networks = getClient().networks().list().execute();
            return map(networks.getList());
        } catch (RuntimeException e) {
            throw new EngineException(EngineError.PROVIDER_FAILURE, e);
        }
    }

    @Override
    public List<ExternalSubnet> getAllSubnets(ProviderNetwork network) {
        List<ExternalSubnet> result = new ArrayList<>();
        Subnets subnets = getClient().subnets().list().execute();
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
        try {
            getClient().subnets().create(subnetForCreate).execute();
        } catch (RuntimeException e) {
            throw new EngineException(EngineError.PROVIDER_FAILURE, e);
        }
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
        try {
            getClient().subnets().delete(id).execute();
        } catch (RuntimeException e) {
            throw new EngineException(EngineError.PROVIDER_FAILURE, e);
        }
    }

    @Override
    public void testConnection() {
        try {
            getClient().execute(new OpenStackRequest<>(getClient(), HttpMethod.GET, "", null, ApiRootResponse.class));
        }  catch (OpenStackResponseException e) {
            log.error("{} (OpenStack response error code: {})", e.getMessage(), e.getStatus());
            log.debug("Exception", e);
            throw new EngineException(EngineError.PROVIDER_FAILURE, e);
        }catch (RuntimeException e) {
            throw new EngineException(EngineError.PROVIDER_FAILURE, e);
        }
    }

    /**
     * Currently, SSL is not supported by Openstack Network Providers.
     */
    @Override
    public List<? extends Certificate> getCertificateChain() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onAddition() {
    }

    @Override
    public void onModification() {
    }

    @Override
    public void onRemoval() {
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
    public Map<String, String> allocate(Network network, VnicProfile vnicProfile, VmNic nic, VDS host) {
        try {
            Port port = locatePort(nic);

            List<String> securityGroups = getSecurityGroups(vnicProfile);
            String hostId = getHostId(host);

            if (port == null) {
                com.woorea.openstack.quantum.model.Network externalNetwork =
                        getExternalNetwork(network.getProvidedBy());
                Port portForCreate = createNewPortForAllocate(nic,
                    securityGroups, hostId, externalNetwork);
                port = getClient().ports().create(portForCreate).execute();
            } else {
                boolean securityGroupsChanged = securityGroupsChanged(port.getSecurityGroups(), securityGroups);
                boolean hostChanged = hostChanged(port, hostId);

                if (securityGroupsChanged || hostChanged) {
                    List<String> modifiedSecurityGroups = securityGroupsChanged ?
                            securityGroups : port.getSecurityGroups();
                    Port portForUpdate = modifyPortForAllocate(port,
                            hostId, hostChanged, modifiedSecurityGroups);
                    port = getClient().ports().update(portForUpdate).execute();
                }
            }
            Map<String, String> runtimeProperties = createPortAllocationRuntimeProperties(port);

            return runtimeProperties;
        } catch (RuntimeException e) {
            throw new EngineException(EngineError.PROVIDER_FAILURE, e);
        }
    }

    private String getHostId(VDS host) {
        if (host.getStaticData().getOpenstackNetworkProviderId() == null) {
            return host.getHostName();
        } else {
            return NetworkUtils.getUniqueHostName(host);
        }
    }

    protected Port modifyPortForAllocate(Port port, String hostId, boolean hostChanged,
            List<String> modifiedSecurityGroups) {
        Port portForUpdate = new PortForUpdate();
        portForUpdate.setId(port.getId());
        portForUpdate.setSecurityGroups(modifiedSecurityGroups);

        if (hostChanged) {
            portForUpdate.setBinding(new Binding());
            portForUpdate.getBinding().setHostId(hostId);
        }
        return portForUpdate;
    }

    protected Port createNewPortForAllocate(VmNic nic,
            List<String> securityGroups, String hostId,
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
        portForCreate.getBinding().setHostId(hostId);
        portForCreate.setTenantId(externalNetwork.getTenantId());
        return portForCreate;
    }

    protected Map<String, String> createPortAllocationRuntimeProperties(Port port) {
        Map<String, String> runtimeProperties = new HashMap<>();
        runtimeProperties.put("vnic_id", port.getId());
        String providerType = provider.getType().name();
        runtimeProperties.put("provider_type", providerType);
        if (port.getSecurityGroups() != null && !port.getSecurityGroups().isEmpty()) {
            runtimeProperties.put("security_groups", StringUtils.join(port.getSecurityGroups(), ','));
        }

        return runtimeProperties;
    }

    private com.woorea.openstack.quantum.model.Network getExternalNetwork(ProviderNetwork providerNetwork) {
        return getClient().networks().show(providerNetwork.getExternalId()).execute();
    }

    private boolean hostChanged(Port port, String hostId) {
        return port.getBinding() == null || !StringUtils.equals(port.getBinding().getHostId(), hostId);
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

            return Arrays.asList(securityGroupsString.split(",\\w*"));
        }

        return DEFAULT_SECURITY_GROUP;
    }

    @Override
    public void deallocate(VmNic nic) {
        try {
            Port port = locatePort(nic);

            if (port != null) {
                getClient().ports().delete(port.getId()).execute();
            }
        } catch (RuntimeException e) {
            throw new EngineException(EngineError.PROVIDER_FAILURE, e);
        }
    }

    private Port locatePort(VmNic nic) {
        List<Port> ports = getClient().ports().list().execute().getList();
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
    public ProviderValidator getProviderValidator() {
        return new ProviderValidator(provider);
    }
}
