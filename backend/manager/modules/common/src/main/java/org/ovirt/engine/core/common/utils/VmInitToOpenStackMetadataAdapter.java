package org.ovirt.engine.core.common.utils;

import static org.ovirt.engine.core.common.utils.ObjectUtils.isEmpty;
import static org.ovirt.engine.core.compat.StringHelper.isNotNullOrEmpty;
import static org.ovirt.engine.core.compat.StringHelper.isNullOrEmpty;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmInitNetwork;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.errors.EngineMessage;

/**
 * Adapter from a vm's network init configuration to the Openstack Metadata protocol.
 */
@Singleton
public class VmInitToOpenStackMetadataAdapter {

    public VmInitToOpenStackMetadataAdapter() {
    }

    /**
     * Convert a network configuration bean to an
     * Openstack Metadata Service format consumed by cloud-init
     */
    public Map<String, Object> asMap(VmInit vmInit) {
        if (vmInit == null) {
            return null;
        }
        List<VmInitNetwork> vmInitNetworks = vmInit.getNetworks();
        List<Map<String, Object>> links = null;
        List<Map<String, Object>> networks = null;

        if (vmInitNetworks != null) {
            links = new LinkedList<>();
            networks  = new LinkedList<>();

            for (VmInitNetwork vmInitNetwork: vmInitNetworks) {
                if (!isStartOnBoot(vmInitNetwork)) {
                    throw new IllegalArgumentException("'Start on boot' must be true");
                }
                boolean addLink = false;
                Map<String, Object> networkIPv4 = mapIPv4(vmInitNetwork);
                if (networkIPv4 != null) {
                    networks.add(networkIPv4);
                    addLink = true;
                }
                Map<String, Object> networkIPv6 = mapIPv6(vmInitNetwork);
                if (networkIPv6 != null) {
                    networks.add(networkIPv6);
                    addLink = true;
                }
                if (addLink) {
                    links.add(mapVifLink(vmInitNetwork));
                }
            }
        }

        List<Map<String, Object>> services = mapServices(vmInit);

        Map<String, Object> networkData = new HashMap<>();
        if (!isEmpty(links)) {
            networkData.put("links", links);
        }
        if (!isEmpty(networks)) {
            networkData.put("networks", networks);
        }
        if (!isEmpty(services)) {
            networkData.put("services", services);
        }
        return networkData;
    }

    private Map<String, Object> mapIPv4(VmInitNetwork vmInitNetwork) {
        if (vmInitNetwork.getBootProtocol() == Ipv4BootProtocol.NONE) {
            return null;
        }
        if (isStaticIPv4AndAddressMissing(vmInitNetwork)) {
            throw new IllegalArgumentException("IPv4 address must be supplied for a static IPv4 configuration");
        }

        Map<String, Object> networkIPv4 = new HashMap<>();
        networkIPv4.put("id", vmInitNetwork.getName());
        networkIPv4.put("link", vmInitNetwork.getName());

        if (vmInitNetwork.getBootProtocol() == Ipv4BootProtocol.DHCP) {
            networkIPv4.put("type", "ipv4_dhcp");
        }
        else if (vmInitNetwork.getBootProtocol() == Ipv4BootProtocol.STATIC_IP) {
            networkIPv4.put("type", "ipv4");
            networkIPv4.put("ip_address", vmInitNetwork.getIp());
            if (isNotNullOrEmpty(vmInitNetwork.getNetmask())) {
                networkIPv4.put("netmask", vmInitNetwork.getNetmask());
            }
            if (isNotNullOrEmpty(vmInitNetwork.getGateway())) {
                networkIPv4.put("gateway", vmInitNetwork.getGateway());
            }
        }

        return networkIPv4;
    }

    private Map<String, Object> mapIPv6(VmInitNetwork vmInitNetwork) {
        if (vmInitNetwork.getIpv6BootProtocol() == Ipv6BootProtocol.NONE) {
            return null;
        }
        else if (isAutoConfIPv6(vmInitNetwork)) {
            throw new IllegalArgumentException("'Stateless address autoconfiguration' (auto conf) is not supported for virtual machines");
        }
        else if (isStaticIPv6AndAddressMissing(vmInitNetwork)) {
            throw new IllegalArgumentException("IPv6 address must be supplied for a static IPv6 configuration");
        }

        Map<String, Object>  networkIPv6 = new HashMap<>();
        networkIPv6.put("id", vmInitNetwork.getName());
        networkIPv6.put("link", vmInitNetwork.getName());

        if (vmInitNetwork.getIpv6BootProtocol() == Ipv6BootProtocol.DHCP) {
            networkIPv6.put("type", "ipv6_dhcp");
        }
        else if (vmInitNetwork.getIpv6BootProtocol() == Ipv6BootProtocol.STATIC_IP) {
            networkIPv6.put("type", "ipv6");
            networkIPv6.put("ip_address", vmInitNetwork.getIpv6Address());
            if (vmInitNetwork.getIpv6Prefix() != null) {
                networkIPv6.put("netmask", String.valueOf(vmInitNetwork.getIpv6Prefix()));
            }
            if (isNotNullOrEmpty(vmInitNetwork.getIpv6Gateway())) {
                networkIPv6.put("gateway", vmInitNetwork.getIpv6Gateway());
            }
        }

        return networkIPv6;
    }

    private Map<String, Object> mapVifLink(VmInitNetwork vmInitNetwork) {
        Map<String, Object> link = new HashMap<>();
        link.put("id", vmInitNetwork.getName());
        link.put("name", vmInitNetwork.getName());
        link.put("type", "vif");
        return link;
    }

    private List<Map<String, Object>> mapServices(VmInit vmInit) {
        List<Map<String, Object>> services = new LinkedList<>();
        if (vmInit.getDnsServers() != null) {
            String[] dnsServers = vmInit.getDnsServers().split(" ");
            if (dnsServers.length > 0) {
                for (String dnsServer : dnsServers) {
                    Map<String, Object> service = new HashMap<>();
                    service.put("type", "dns-nameserver");
                    service.put("address", dnsServer);
                    services.add(service);
                }
            }
        }

        if (vmInit.getDnsSearch() != null) {
            String[] dnsSearch = vmInit.getDnsSearch().split(" ");
            if (dnsSearch.length > 0) {
                for (String dnsServer : dnsSearch) {
                    Map<String, Object> service = new HashMap<>();
                    service.put("type", "dns-search");
                    service.put("address", dnsServer);
                    services.add(service);
                }
            }
        }
        return services;
    }

    public List<EngineMessage> validate(VmInit vmInit) {
        if (vmInit == null || vmInit.getNetworks() == null) {
            return null;
        }

        List<EngineMessage> msgs = new LinkedList<>();
        List<VmInitNetwork> vmInitNetworks = vmInit.getNetworks();
        for (VmInitNetwork vmInitNetwork : vmInitNetworks) {
            if (!isStartOnBoot(vmInitNetwork)) {
                msgs.add(EngineMessage.VALIDATION_CLOUD_INIT_START_ON_BOOT_INVALID);
            }
            if (isStaticIPv4AndAddressMissing(vmInitNetwork)) {
                msgs.add(EngineMessage.VALIDATION_CLOUD_INIT_STATIC_IPV4_ADDRESS_MISSING);
            }
            if (isStaticIPv6AndAddressMissing(vmInitNetwork)) {
                msgs.add(EngineMessage.VALIDATION_CLOUD_INIT_STATIC_IPV6_ADDRESS_MISSING);
            }
            if (isAutoConfIPv6(vmInitNetwork)) {
                msgs.add(EngineMessage.VALIDATION_CLOUD_INIT_IPV6_AUTOCONF_UNSUPPORTED);
            }
        }
        return msgs;
    }

    private boolean isStartOnBoot(VmInitNetwork vmInitNetwork) {
        return vmInitNetwork.getStartOnBoot() != null && vmInitNetwork.getStartOnBoot();
    }

    private boolean isAutoConfIPv6(VmInitNetwork vmInitNetwork) {
        return vmInitNetwork.getIpv6BootProtocol() == Ipv6BootProtocol.AUTOCONF;
    }

    private boolean isStaticIPv4AndAddressMissing(VmInitNetwork vmInitNetwork) {
        return vmInitNetwork.getBootProtocol() == Ipv4BootProtocol.STATIC_IP && isNullOrEmpty(vmInitNetwork.getIp());
    }

    private boolean isStaticIPv6AndAddressMissing(VmInitNetwork vmInitNetwork) {
        return vmInitNetwork.getIpv6BootProtocol() == Ipv6BootProtocol.STATIC_IP && isNullOrEmpty(vmInitNetwork.getIpv6Address());
    }
}
